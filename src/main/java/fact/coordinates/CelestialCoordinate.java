package fact.coordinates;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by maxnoe on 23.05.17.
 *
 * Interface for celestial coordinates. Provides
 * the greatCircleDistance and the conversion from ZonedDateTime to Greenwich Sidereal Time
 */
public interface CelestialCoordinate extends Serializable {

    ZonedDateTime J2000_REFERENCE_DATA = ZonedDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
    double JULIAN_CENTURY = 36525;

    /**
     * Great circle distance using the more complicated but numerically more
     * stable version of wikipedia (special case of the Vincenty formula)
     * https://en.wikipedia.org/wiki/Great-circle_distance
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Great circle distance between the two points
     */
    static double greatCircleDistance(double lat1, double lon1, double lat2, double lon2) {

        double deltaLon = lon1 - lon2;

        double t1 = Math.cos(lat2) * Math.sin(deltaLon);
        double t2 = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double numerator = Math.sqrt(Math.pow(t1, 2.0) + Math.pow(t2, 2.0));

        double t3 = Math.sin(lat1) * Math.sin(lat2);
        double t4 = Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double denominator = t3 + t4;

        return Math.atan2(numerator, denominator);
    }



    /**
     * Convert a DateTime object to Greenwich Sidereal Time according to
     * https://en.wikipedia.org/wiki/Sidereal_time#Definition
     * @param datetime
     * @return gst in radians
     */
    static double datetimeToGST(ZonedDateTime datetime){

        Duration difference = Duration.between(J2000_REFERENCE_DATA, datetime);
        double difference_seconds = difference.getSeconds() + difference.getNano() / 1e9;
        double gst = 18.697374558 + 24.06570982441908 * (difference_seconds / 86400.0);

        // normalize to [0, 24] and convert to radians
        while (gst < 0) {
            gst += 24;
        }
        gst = (gst % 24) / 12.0 * Math.PI;
        return gst;
    }

    class Nutation {
        public final double Δψ;
        public final double Δε;

        Nutation(double Δψ, double Δε) {
            this.Δψ = Δψ;
            this.Δε = Δε;
        }
    }

    static Nutation approximateEarthNutation(ZonedDateTime obsTime){
        Duration duration = Duration.between(J2000_REFERENCE_DATA, obsTime);
        double days = (duration.getSeconds() + duration.getNano() / 1e9) / (3600 * 24);
        double T = days / JULIAN_CENTURY;
        double Ω = Math.toRadians(125.04452 - 1934.136261 * T);
        double L = Math.toRadians(280.4665 + 36000.7698 * T);
        double N = Math.toRadians(218.3165 + 481267.8813 * T);
        double sΩ = Math.sin(Ω);
        double cΩ = Math.cos(Ω);
        double s2L = Math.sin(2 * L);
        double c2L = Math.cos(2 * L);
        double s2N = Math.sin(2 * N);
        double c2N = Math.cos(2 * N);
        double s2Ω = Math.sin(2 * Ω);
        double c2Ω = Math.cos(2 * Ω);


        double Δψ = Math.toRadians((-17.2 * sΩ - 1.32 * s2L - 0.23 * s2N + 0.21 * s2Ω) / 3600);
        double Δε = Math.toRadians((9.2 * cΩ + 0.57 * c2L + 0.1 * c2N - 0.09 * c2Ω) / 3600);

        return new Nutation(Δψ, Δε);
    }
}
