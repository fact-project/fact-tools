package fact.coordinates;

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

    /**
     * Returns position of the source in the camera from the given pointing position
     * Code by M. Nöthe
     * @return
     */
    public CameraCoordinate toCamera(HorizontalCoordinate pointingPosition, double focalLength)
    {

        double paz = Math.toRadians(pointingPosition.getAzimuthRad());
        double pzd = Math.toRadians(pointingPosition.getZenithRad());
        double saz = Math.toRadians(this.getAzimuthRad());
        double szd = Math.toRadians(this.getZenithRad());

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
