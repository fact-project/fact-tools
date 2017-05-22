package fact.coordinates;

import java.time.ZonedDateTime;

/**
 * Created by maxnoe on 22.05.17.
 */
public class HorizontalCoordinate {

    private final double zenithRad;
    private final double azimuthRad;

    public HorizontalCoordinate(double zenithRad, double azimuthRad) {
        this.zenithRad = zenithRad;
        this.azimuthRad = azimuthRad;
    }

    public static HorizontalCoordinate fromDegrees(double zenithDeg, double azimuthDeg) {
        return new HorizontalCoordinate(Math.toRadians(zenithDeg), Math.toRadians(azimuthDeg));
    }

    public double greatCircleDistance(HorizontalCoordinate other) {
        return Utils.greatCircleDistance(
                this.getAltitudeRad(), this.getAzimuthRad(),
                other.getAltitudeRad(), other.getAzimuthRad()
        );
    }


    /**
     * Implementation of the formulas from
     * https://en.wikipedia.org/wiki/Celestial_coordinate_system#Equatorial_.E2.86.90.E2.86.92_horizontal
     *
     * @param observationTime
     * @param earthLocation
     * @return HorizontalCoordinate for given time and location;
     */
    public EquatorialCoordinate toEquatorial(ZonedDateTime observationTime, EarthLocation earthLocation) {

        double gst = Utils.datetimeToGST(observationTime);
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

        return new EquatorialCoordinate(ra, declination);
    }

    /**
     * Returns position of the source in the camera from the given pointing position
     * Code by M. Nöthe
     * @return
     */
    public CameraCoordinate toCamera(HorizontalCoordinate pointingPosition, double focalLength)
    {

        double paz = pointingPosition.getAzimuthRad();
        double pzd = pointingPosition.getZenithRad();
        double saz = this.getAzimuthRad();
        double szd = this.getZenithRad();

        double x = Math.sin(szd) * Math.cos(saz);
        double y = Math.sin(szd) * Math.sin(saz);
        double z = Math.cos(szd);

        double x_rot = -Math.sin(-pzd) * z - Math.cos(-pzd) * (Math.cos(-paz) * x - Math.sin(-paz) * y);
        double y_rot =  Math.sin(-paz) * x + Math.cos(-paz) * y;
        double z_rot =  Math.cos(-pzd) * z - Math.sin(-pzd) * (Math.cos(-paz) * x - Math.sin(-paz) * y);

        return new CameraCoordinate(x_rot * (-focalLength) / z_rot, - y_rot * (-focalLength) / z_rot);
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
