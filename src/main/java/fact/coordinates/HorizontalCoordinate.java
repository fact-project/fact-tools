package fact.coordinates;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.time.ZonedDateTime;

/**
 * Represents a coordinate in the horizontal coordinate.
 *
 * Provides a method to transform into the equatorial and camera frame.
 *
 * Created by maxnoe on 22.05.17.
 */
public class HorizontalCoordinate implements CelestialCoordinate {

    private final double zenithRad;
    private final double azimuthRad;

    private HorizontalCoordinate(double zenithRad, double azimuthRad) {
        this.zenithRad = zenithRad;
        this.azimuthRad = azimuthRad;
    }

    public static HorizontalCoordinate fromRad(double zenithRad, double azimuthRad) {
        return new HorizontalCoordinate(zenithRad, azimuthRad);
    }

    public static HorizontalCoordinate fromDegrees(double zenithDeg, double azimuthDeg) {
        return new HorizontalCoordinate(Math.toRadians(zenithDeg), Math.toRadians(azimuthDeg));
    }

    public double greatCircleDistance(HorizontalCoordinate other) {
        return CelestialCoordinate.greatCircleDistance(
                this.getAltitudeRad(), this.getAzimuthRad(),
                other.getAltitudeRad(), other.getAzimuthRad()
        );
    }


    /**
     * Transform this HorizontalCoordinate into the equatorial coordinate frame
     * for given observation time and location.
     *
     * Implementation of the formulas from
     * https://en.wikipedia.org/wiki/Celestial_coordinate_system#Equatorial_.E2.86.90.E2.86.92_horizontal
     *
     * @param observationTime
     * @param earthLocation
     * @return HorizontalCoordinate for given time and location;
     */
    public EquatorialCoordinate toEquatorial(ZonedDateTime observationTime, EarthLocation earthLocation) {

        double gst = CelestialCoordinate.datetimeToGST(observationTime);
        double azimuthSouth = this.getAzimuthRad() - Math.PI;
        double alt = this.getAltitudeRad();

        double lat = earthLocation.getLatitudeRad();
        double lon = earthLocation.getLongitudeRad();

        double hourAngle = Math.atan2(
                Math.sin(azimuthSouth),
                Math.cos(azimuthSouth) * Math.sin(lat) + Math.tan(alt) * Math.cos(lat)
        );

        double declination = Math.asin(Math.sin(lat) * Math.sin(alt) - Math.cos(lat) * Math.cos(alt) * Math.cos(azimuthSouth));

        double ra = gst + lon - hourAngle;

        return EquatorialCoordinate.fromRad(ra, declination);
    }

    /**
     * Transform this horizontal coordinate to the camera frame for the given pointing position and focal length.
     * @param pointingPosition Pointing of the telescope
     * @param focalLength focalLength of the telescope
     * @return coordinate transformed into the camera frame
     */
    public CameraCoordinate toCamera(HorizontalCoordinate pointingPosition, double focalLength)
    {

        double paz = pointingPosition.getAzimuthRad();
        double pzd = pointingPosition.getZenithRad();
        double saz = this.getAzimuthRad();
        double szd = this.getZenithRad();

        Vector3D vec = new Vector3D(Math.sin(szd) * Math.cos(saz), Math.sin(szd) * Math.sin(saz), Math.cos(szd));

        Rotation rotZAz = new Rotation(new Vector3D(0.0, 0.0, 1.0), -paz, RotationConvention.VECTOR_OPERATOR);
        Rotation rotYZd = new Rotation(new Vector3D(0.0, 1.0, 0.0), -pzd, RotationConvention.VECTOR_OPERATOR);

        Vector3D rotVec = rotYZd.applyTo(rotZAz.applyTo(vec));

        double x = rotVec.getX();
        double y = rotVec.getY();
        double z = rotVec.getZ();

        CameraCoordinate cameraCoordinate = new CameraCoordinate(x * (focalLength) / z, y * (focalLength) / z);

        return cameraCoordinate;
    }

    public String toString(){
        return String.format("HorizontalCoordinate(zd=%.4f°, az=%.4f°)", this.getZenithDeg(), this.getAzimuthDeg());
    }

    public double getAltitudeRad() {
        return Math.PI / 2 - zenithRad;
    }

    public double getAltitudeDeg() {
        return 90 - this.getZenithDeg();
    }

    public double getZenithRad() {
        return zenithRad;
    }

    public double getAzimuthRad() {
        return azimuthRad;
    }

    public double getZenithDeg() {
        return Math.toDegrees(zenithRad);
    }

    public double getAzimuthDeg() {
        return Math.toDegrees(azimuthRad);
    }
}
