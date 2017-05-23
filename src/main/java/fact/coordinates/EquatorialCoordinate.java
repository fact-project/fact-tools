package fact.coordinates;

import java.time.ZonedDateTime;

/**
 * Created by maxnoe on 22.05.17.
 */
public class EquatorialCoordinate implements CelestialCoordinate {

    private final double rightAscensionRad;
    private final double declinationRad;

    public EquatorialCoordinate(double rightAscensionRad, double declinationRad) {
        this.rightAscensionRad = rightAscensionRad;
        this.declinationRad = declinationRad;
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
     * Implementation of the formulas from
     * https://en.wikipedia.org/wiki/Celestial_coordinate_system#Equatorial_.E2.86.90.E2.86.92_horizontal
     *
     * @param observationTime
     * @param earthLocation
     * @return HorizontalCoordinate for given time and location;
     */
    public HorizontalCoordinate toHorizontal(ZonedDateTime observationTime, EarthLocation earthLocation) {

        double gst = CoordinateUtils.datetimeToGST(observationTime);

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

        return new HorizontalCoordinate(Math.PI / 2.0 - altitude, azimuth);
    }

    public String toString(){
        return String.format("HorizontalCoordinate(ra=%.4f ha, dec=%.4f°)", this.getRightAscensionHA(), this.getDeclinationDeg());
    }

    public double greatCircleDistance(EquatorialCoordinate other) {
        return CelestialCoordinate.greatCircleDistance(
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
