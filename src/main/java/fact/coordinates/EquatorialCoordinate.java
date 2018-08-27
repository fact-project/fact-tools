package fact.coordinates;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by maxnoe on 22.05.17.
 * <p>
 * Represents a celestial coordinate in the equatorial coordinate frame
 * using right ascension and declination.
 * <p>
 * Provides a method to convert to the horizontal coordinate frame
 */
public class EquatorialCoordinate implements CelestialCoordinate {

    public final double rightAscensionRad;
    public final double declinationRad;
    public static final ZonedDateTime j2000Reference = ZonedDateTime.of(2000, 1, 1, 11, 58, 55, 816000000, ZoneOffset.UTC);
    public static final double precessionFactorM = 3.075 / 3600 / 12 * Math.PI;
    public static final double precessionFactorN = 1.336 / 3600 / 12 * Math.PI;

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
     * Calculate the corrections for right ascension in declination
     * that are needed because of Earth's precession.
     * <p>
     * The formulas are approximations found here
     * http://www.cv.nrao.edu/~rfisher/Ephemerides/earth_rot.html
     * <p>
     * TODO Check with the "Explanatory Supplements for the Astronomical Almanac"
     *
     * @param rightAscensionRad
     * @param declinationRad
     * @param observationTime
     * @return correction for right ascension, declination in radians
     */
    static double[] calculatePrecessionCorrection(double rightAscensionRad, double declinationRad, ZonedDateTime observationTime) {
        double deltaT = observationTime.until(j2000Reference, ChronoUnit.SECONDS) / 365.0 / 86400.0;
        double deltaRightAscension = (precessionFactorM + precessionFactorN * Math.sin(rightAscensionRad) * Math.tan(declinationRad)) * deltaT;
        double deltaDeclination = precessionFactorN * Math.cos(rightAscensionRad) * deltaT;
        return new double[]{deltaRightAscension, deltaDeclination};
    }

    /**
     * Transform this EquatorialCoordinate into the horizontal coordinate frame
     * for given observation time and location.
     * <p>
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

        double[] precessionCorrection = calculatePrecessionCorrection(ra, dec, observationTime);
        ra -= precessionCorrection[0];
        dec -= precessionCorrection[1];

        // wikipedia assumes longitude positive in west direction
        double hourAngle = gst + earthLocation.longitudeRad - ra;

        double altitude = Math.asin(
                Math.sin(earthLocation.latitudeRad) * Math.sin(dec) +
                        Math.cos(earthLocation.latitudeRad) * Math.cos(dec) * Math.cos(hourAngle)
        );

        double azimuth = Math.atan2(
                Math.sin(hourAngle),
                Math.cos(hourAngle) * Math.sin(earthLocation.latitudeRad) - Math.tan(dec) * Math.cos(earthLocation.latitudeRad)
        );

        // azimuth starting in the north
        azimuth -= Math.PI;

        // normalize azimuth to (-pi, pi]
        if (azimuth <= -Math.PI) {
            azimuth += 2 * Math.PI;
        }

        return HorizontalCoordinate.fromRad(Math.PI / 2.0 - altitude, azimuth);
    }

    public String toString() {
        return String.format("HorizontalCoordinate(ra=%.4f ha, dec=%.4f°)", this.getRightAscensionHA(), this.getDeclinationDeg());
    }

    /**
     * Return the angular great circle distance in radians
     * between this EquatorialCoordinate and another
     *
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
