package fact.coordinates;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.io.Serializable;

/**
 * Created by maxnoe on 22.05.17.
 */
public class CameraCoordinate implements Serializable {
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

        Rotation rotZAz = new Rotation(new Vector3D(0.0, 0.0, 1.0), -paz, RotationConvention.VECTOR_OPERATOR);
        Rotation rotYZd = new Rotation(new Vector3D(0.0, 1.0, 0.0), -pzd, RotationConvention.VECTOR_OPERATOR);

        Vector3D rotVec = rotZAz.applyInverseTo(rotYZd.applyInverseTo(vec));

        double zenith = Math.acos(rotVec.getZ());
        double azimuth = Math.atan2(rotVec.getY(), rotVec.getX());

        return new HorizontalCoordinate(zenith, azimuth);
    }

    public double euclideanDistance(CameraCoordinate other) {
        double dx = this.getXMM() - other.getXMM();
        double dy = this.getYMM() - other.getYMM();
        return Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dy, 2.0));
    }



    public String toString(){
        return String.format("CameraCoordinate(x=%.4f mm, y=%.4f mm)", this.xMM, this.yMM);
    }
}
