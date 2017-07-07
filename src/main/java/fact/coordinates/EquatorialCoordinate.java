package fact.coordinates;

import java.time.ZonedDateTime;

/**
 * Created by maxnoe on 22.05.17.
 *
 * Represents a celestial coordinate in the equatorial coordinate frame
 * using right ascension and declination.
 *
 * Provides a method to convert to the horizontal coordinate frame
 */
public class EquatorialCoordinate implements CelestialCoordinate {

    public final double rightAscensionRad;
    public final double declinationRad;

    private EquatorialCoordinate(double rightAscensionRad, double declinationRad) {
        this.rightAscensionRad = rightAscensionRad;
        this.declinationRad = declinationRad;
    }

    public static EquatorialCoordinate fromRad(double rightAscensionRad, double declinationRad) {
        return new EquatorialCoordinate(rightAscensionRad, declinationRad);
    }

    public static EquatorialCoordinate fromDegrees(double rightAscensionDeg, double declinationDeg) {
        return new EquatorialCoordinate(Math.toRadians(rightAscensionDeg), Math.toRadians(declinationDeg));
    }

    public static EquatorialCoordinate fromHourAngleAndRad(double rightAscensionHA, double declinationRad) {
        double rightAscensionRad = rightAscensionHA / 12.0 * Math.PI;
        return new EquatorialCoordinate(rightAscensionRad, declinationRad);
    }

    public static EquatorialCoordinate fromHourAngleAndDegrees(double rightAscensionHA, double declinationDeg) {
        double rightAscensionRad = rightAscensionHA / 12.0 * Math.PI;
        return new EquatorialCoordinate(rightAscensionRad, Math.toRadians(declinationDeg));
    }


    /**
     * Transform this EquatorialCoordinate into the horizontal coordinate frame
     * for given observation time and location.
     *
     * Implementation of the formulas from
     * https://en.wikipedia.org/wiki/Celestial_coordinate_system#Equatorial_.E2.86.90.E2.86.92_horizontal
     *
     * @param observationTime
     * @param earthLocation
     * @return HorizontalCoordinate for given time and location;
     */
    public HorizontalCoordinate toHorizontal(ZonedDateTime observationTime, EarthLocation earthLocation) {

        double gst = CelestialCoordinate.datetimeToGST(observationTime);

        double ra = this.getRightAscensionRad();
        double dec = this.getDeclinationRad();


        // wikipedia assumes longitude positive in west direction
        double hourAngle = gst + earthLocation.getLongitudeRad() - ra;

        double altitude = Math.asin(
                Math.sin(earthLocation.getLatitudeRad()) * Math.sin(dec) +
                        Math.cos(earthLocation.getLatitudeRad()) * Math.cos(dec) * Math.cos(hourAngle)
        );

        double azimuth = Math.atan2(
                Math.sin(hourAngle),
                Math.cos(hourAngle) * Math.sin(earthLocation.getLatitudeRad()) - Math.tan(dec) * Math.cos(earthLocation.getLatitudeRad())
        );

        // azimuth starting in the north
        azimuth -= Math.PI;

        // normalize azimuth to (-pi, pi]
        if (azimuth <= -Math.PI) {
            azimuth += 2 * Math.PI;
        }

        return HorizontalCoordinate.fromRad(Math.PI / 2.0 - altitude, azimuth);
    }

    public String toString(){
        return String.format("HorizontalCoordinate(ra=%.4f ha, dec=%.4f°)", this.getRightAscensionHA(), this.getDeclinationDeg());
    }

    /**
     * Return the angular great circle distance in radians
     * between this EquatorialCoordinate and another
     * @param other
     * @return Angular great circle distance in radians
     */
    public double greatCircleDistanceRad(EquatorialCoordinate other) {
        return CelestialCoordinate.greatCircleDistanceRad(
                this.getDeclinationRad(), this.getRightAscensionRad(),
                other.getDeclinationRad(), other.getRightAscensionRad()
        );
    }

    public double greatCircleDistanceDeg(EquatorialCoordinate other) {
        return CelestialCoordinate.greatCircleDistanceDeg(
                this.getDeclinationRad(), this.getRightAscensionRad(),
                other.getDeclinationRad(), other.getRightAscensionRad()
        );
    }

    public double getRightAscensionRad() {
        return rightAscensionRad;
    }

    public double getDeclinationRad() {
        return declinationRad;
    }

    public double getRightAscensionDeg() {
        return Math.toDegrees(rightAscensionRad);
    }

    public double getRightAscensionHA() {
        return rightAscensionRad / Math.PI * 12;
    }

    public double getDeclinationDeg() {
        return Math.toDegrees(declinationRad);
    }
}
