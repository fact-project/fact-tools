package fact.coordinates;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by maxnoe on 23.05.17.
 * <p>
 * Interface for celestial coordinates. Provides
 * the greatCircleDistance and the conversion from ZonedDateTime to Greenwich Sidereal Time
 */
public interface CelestialCoordinate extends Serializable {

    ZonedDateTime gstReferenceDateTime = ZonedDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"));


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
    static double greatCircleDistanceRad(double lat1, double lon1, double lat2, double lon2) {

        double deltaLon = lon1 - lon2;

        double t1 = Math.cos(lat2) * Math.sin(deltaLon);
        double t2 = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double numerator = Math.sqrt(Math.pow(t1, 2.0) + Math.pow(t2, 2.0));

        double t3 = Math.sin(lat1) * Math.sin(lat2);
        double t4 = Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double denominator = t3 + t4;

        return Math.atan2(numerator, denominator);
    }

    static double greatCircleDistanceDeg(double lat1, double lon1, double lat2, double lon2) {
        return Math.toDegrees(greatCircleDistanceRad(lat1, lon1, lat2, lon2));
    }


    /**
     * Convert a DateTime object to Greenwich Sidereal Time according to
     * https://en.wikipedia.org/wiki/Sidereal_time#Definition
     *
     * @param datetime
     * @return gst in radians
     */
    static double datetimeToGST(ZonedDateTime datetime) {

        Duration difference = Duration.between(gstReferenceDateTime, datetime);
        double difference_seconds = difference.getSeconds() + difference.getNano() / 1e9;
        double gst = 18.697374558 + 24.06570982441908 * (difference_seconds / 86400.0);

        // normalize to [0, 24] and convert to radians
        while (gst < 0) {
            gst += 24;
        }
        gst = (gst % 24) / 12.0 * Math.PI;
        return gst;
    }
}
