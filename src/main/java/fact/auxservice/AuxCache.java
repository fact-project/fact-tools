package fact.auxservice;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;

/**
 * This class defines some classes needed for caching many {@link AuxPoint} into ram
 * using an LRU cache. A specific auxfile is uniquely defined by its name and the 'FACT Night' string.
 * You know...the typical 20160320-like string.
 * <p>
 * Created by kai on 12.06.16.
 */

public class AuxCache {
    public class CacheKey {
        final AuxiliaryServiceName service;
        final Integer factNight;
        public final Path path;
        public final String filename;


        public CacheKey(AuxiliaryServiceName service, ZonedDateTime timeStamp) {
            this.service = service;
            this.factNight = dateTimeStampToFACTNight(timeStamp);

            filename = factNight + "." + service + ".fits";
            this.path = Paths.get(dateTimeStampToFACTPath(timeStamp).toString(), filename);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            return service == cacheKey.service && factNight.equals(cacheKey.factNight);

        }

        @Override
        public int hashCode() {
            int result = service.hashCode();
            result = 31 * result + factNight.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "CacheKey{" +
                    "service=" + service +
                    ", factNight=" + factNight +
                    '}';
        }
    }

    /**
     * Takes a dateTime object and returns the appropriate FACT night number.
     * Entering some datetime for 9:30 in the morning, e.g. 2016-01-03 09:30:12, returns 20160102.
     * The intervall goes to 12:00 noon before its switched to the next night.
     *
     * @param timestamp the timestamp to get the night for
     * @return a FACT night number.
     */
    public static Integer dateTimeStampToFACTNight(ZonedDateTime timestamp) {
        ZonedDateTime offsetDate = timestamp.minusHours(12);
        String night = String.format("%1$d%2$02d%3$02d", offsetDate.getYear(), offsetDate.getMonthValue(), offsetDate.getDayOfMonth());
        return Integer.parseInt(night);
    }


    /**
     * Takes a dateTime object and returns the canonical path to an aux or data file.
     * For example 2016-01-03 09:30:12 returns a path to "2016/01/02" while
     * 2016-01-03 13:30:12 would return "2016/01/03"
     *
     * @param timeStamp the timestamp to get the night for
     * @return a partial path starting with the year.
     */
    public static Path dateTimeStampToFACTPath(ZonedDateTime timeStamp) {
        ZonedDateTime offsetDate = timeStamp.minusHours(12);

        int year = offsetDate.getYear();
        int month = offsetDate.getMonthValue();
        int day = offsetDate.getDayOfMonth();

        return Paths.get(String.format("%04d", year), String.format("%02d", month), String.format("%02d", day));
    }
}
