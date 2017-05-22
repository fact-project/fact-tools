package fact.coordinates;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Created by maxnoe on 22.05.17.
 */
public class CameraCoordinate {
    private final double xMM;
    private final double yMM;

    public CameraCoordinate(double xMM, double yMM){
        this.xMM = xMM;
        this.yMM = yMM;
    }

    public double getXMM() {
        return xMM;
    }

    public double getYMM() {
        return yMM;
    }


    public HorizontalCoordinate toHorizontal(HorizontalCoordinate pointingPosition, double focalLength){

        double paz = pointingPosition.getAzimuthRad();
        double pzd = pointingPosition.getZenithRad();

        double z = 1 / Math.sqrt(1 + Math.pow(this.getXMM() / focalLength, 2.0) + Math.pow(this.getYMM() / focalLength, 2.0));
        double x = this.getXMM() * z / focalLength;
        double y = this.getYMM() * z / focalLength;

        Vector3D vec = new Vector3D(x, y, z);

        Rotation rotZAz = new Rotation(new Vector3D(0.0, 0.0, 1.0), -paz);
        Rotation rotYZd = new Rotation(new Vector3D(0.0, 1.0, 0.0), -pzd);

        Vector3D rotVec = rotZAz.applyInverseTo(rotYZd.applyInverseTo(vec));

        double zenith = Math.acos(rotVec.getZ());
        double azimuth = Math.atan2(rotVec.getY(), rotVec.getX());

        return new HorizontalCoordinate(zenith, azimuth);
    }


    public String toString(){
        return "CameraCoordinate(x=" + this.xMM + ", y=" + this.yMM + ")";
    }
}
