package fact.auxservice;


import org.joda.time.DateTime;

/**
 * Created by kai on 12.06.16.
 */

public class AuxCache {
    class CacheKey {
        final AuxiliaryServiceName service;
        final Integer factNight;

        public CacheKey(AuxiliaryServiceName service, Integer factNight) {
            this.service = service;
            this.factNight = factNight;
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

    public static Integer dateTimeStampToFACTNight(DateTime timestamp){
        DateTime offsetDate = timestamp.minusHours(12);
        String night = String.format("%1$d%2$02d%3$02d", offsetDate.getYear(), offsetDate.getMonthOfYear(), offsetDate.getDayOfMonth());
        return Integer.parseInt(night);
    }
}
