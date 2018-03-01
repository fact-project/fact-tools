package fact.coordinates;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.io.Serializable;

/**
 * Created by maxnoe on 22.05.17.
 * <p>
 * This class represents a coordinate in the camera frame using
 * euclidean coordinates in mm.
 * <p>
 * It provides a method toHorizontal to transform from camera frame to telescope frame
 */
public class CameraCoordinate implements Serializable {
    public final double xMM;
    public final double yMM;

    public CameraCoordinate(double xMM, double yMM) {
        this.xMM = xMM;
        this.yMM = yMM;
    }


    /**
     * Transform this CameraCoordinate from camera frame to telescope (horizontal coordinates) frame
     * for the given PointingPosition.
     *
     * @param pointingPosition the telescope's pointing position
     * @param focalLength      the focal length of the telescope
     * @return the camera coordinate transformed into the horizontal coordinate frame
     */
    public HorizontalCoordinate toHorizontal(HorizontalCoordinate pointingPosition, double focalLength) {

        double paz = pointingPosition.getAzimuthRad();
        double pzd = pointingPosition.getZenithRad();

        double z = 1 / Math.sqrt(1 + Math.pow(xMM / focalLength, 2.0) + Math.pow(yMM / focalLength, 2.0));

        // rotate camera by -90 degrees to invert the rotation
        double x = yMM * z / focalLength;
        double y = -xMM * z / focalLength;

        Vector3D vec = new Vector3D(x, y, z);

        Rotation rotZAz = new Rotation(new Vector3D(0.0, 0.0, 1.0), -paz, RotationConvention.VECTOR_OPERATOR);
        Rotation rotYZd = new Rotation(new Vector3D(0.0, 1.0, 0.0), -pzd, RotationConvention.VECTOR_OPERATOR);

        Vector3D rotVec = rotZAz.applyInverseTo(rotYZd.applyInverseTo(vec));

        double zenith = Math.acos(rotVec.getZ());
        double azimuth = Math.atan2(rotVec.getY(), rotVec.getX());

        return HorizontalCoordinate.fromRad(zenith, azimuth);
    }

    public double euclideanDistance(CameraCoordinate other) {
        double dx = xMM - other.xMM;
        double dy = yMM - other.yMM;
        return Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dy, 2.0));
    }

    public String toString() {
        return String.format("CameraCoordinate(x=%.4f mm, y=%.4f mm)", this.xMM, this.yMM);
    }
}
