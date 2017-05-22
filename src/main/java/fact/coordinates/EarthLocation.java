package fact.coordinates;

/**
 * Created by maxnoe on 22.05.17.
 */
public class EarthLocation {
    final private double latitudeRad;
    final private double longitudeRad;
    final private double altitudeMeter;

    public EarthLocation(double latitudeRad, double longitudeRad, double altitudeMeter) {
        this.latitudeRad = latitudeRad;
        this.longitudeRad = longitudeRad;
        this.altitudeMeter = altitudeMeter;
    }

    public static EarthLocation fromDegrees(double latitudeDeg, double longitudeDeg, double altitudeMeter) {
        double latitudeRad = Math.toRadians(latitudeDeg);
        double longitudeRad = Math.toRadians(longitudeDeg);
        return new EarthLocation(latitudeRad, longitudeRad, altitudeMeter);
    }

    public double getLatitudeRad() {
        return latitudeRad;
    }

    public double getLongitudeRad() {
        return longitudeRad;
    }

    public double getAltitudeMeter() {
        return altitudeMeter;
    }

    public double getLatitudeDeg() {
        return Math.toDegrees(latitudeRad);
    }

    public double getLongitudeDeg() {
        return Math.toDegrees(longitudeRad);
    }
}
