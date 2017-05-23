package fact.coordinates;


import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by maxnoe on 24.02.17.
 */
public class CoordinateUtils {
    // reference datetime
    private static final ZonedDateTime gstReferenceDateTime = ZonedDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"));


    /**
     * Convert a DateTime object to greenwhich sidereal time according to
     * https://en.wikipedia.org/wiki/Sidereal_time#Definition
     * @param datetime
     * @return gst in radians
     */
    public static double datetimeToGST(ZonedDateTime datetime){

        Duration difference = Duration.between(gstReferenceDateTime, datetime);
        double difference_seconds = difference.getSeconds() + difference.getNano() / 1e9;
        double gst = 18.697374558 + 24.06570982441908 * (difference_seconds / 86400.0);

        // normalize to [0, 24] and convert to radians
        gst = (gst % 24) / 12.0 * Math.PI;
        return gst;
    }
}
