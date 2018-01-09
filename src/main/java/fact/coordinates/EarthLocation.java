package fact.coordinates;

/**
 * Created by maxnoe on 22.05.17.
 * <p>
 * Represents a position on Earth using latitude, longitude and height above sea level
 */
public class EarthLocation {
    final public double latitudeRad;
    final public double longitudeRad;
    final public double altitudeMeter;

    // coordinates from google earth
    final static public EarthLocation FACT = EarthLocation.fromDegrees(28.761647, -17.891116, 2200);

    private EarthLocation(double latitudeRad, double longitudeRad, double altitudeMeter) {
        this.latitudeRad = latitudeRad;
        this.longitudeRad = longitudeRad;
        this.altitudeMeter = altitudeMeter;
    }

    public EarthLocation fromRad(double latitudeRad, double longitudeRad, double altitudeMeter) {
        return new EarthLocation(latitudeRad, longitudeRad, altitudeMeter);
    }

    public static EarthLocation fromDegrees(double latitudeDeg, double longitudeDeg, double altitudeMeter) {
        double latitudeRad = Math.toRadians(latitudeDeg);
        double longitudeRad = Math.toRadians(longitudeDeg);
        return new EarthLocation(latitudeRad, longitudeRad, altitudeMeter);
    }

    public double getLatitudeDeg() {
        return Math.toDegrees(latitudeRad);
    }

    public double getLongitudeDeg() {
        return Math.toDegrees(longitudeRad);
    }
}
