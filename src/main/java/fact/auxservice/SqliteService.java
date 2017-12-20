package fact.auxservice;

import com.google.common.cache.*;
import fact.auxservice.strategies.AuxPointStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * This implements an AuxiliaryService {@link AuxiliaryService}  providing data from a sqlite file.
 * Can handle TRACKING and SOURCE information.
 * <p>
 * Created by kai on 01.03.15.
 */
public class SqliteService implements AuxiliaryService {

    static Logger log = LoggerFactory.getLogger(SqliteService.class);

    @Parameter(required = true, description = "The URL to the sqlite database file. ")
    private SourceURL url;

    @Parameter(required = false, description = "Expiry time for entries in the cache.", defaultValue = "10 Minutes")
    private int window = 10;

    private LoadingCache<AuxDataCacheKey, TreeSet<AuxPoint>> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(window, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    log.info("Removing Data {} from cache for cause {}", notification.toString(), notification.getCause());
                }
            })
            .build(new CacheLoader<AuxDataCacheKey, TreeSet<AuxPoint>>() {
                @Override
                public TreeSet<AuxPoint> load(AuxDataCacheKey key) throws Exception {
                    log.info("Building entry for service {} and key: {}", key.service, key.roundedTimeStamp);
                    return loadDataFromDataBase(key.service, key.roundedTimeStamp);
                }
            });

    public static ZonedDateTime floorToQuarterHour(ZonedDateTime time) {
        ZonedDateTime t = time.withZoneSameInstant(ZoneOffset.UTC).withSecond(0);

        int oldMinute = t.getMinute();
        int newMinute = 15 * (int) Math.floor(oldMinute / 15.0);

        return t.withMinute(newMinute);
    }


    public class AuxDataCacheKey {
        private final AuxiliaryServiceName service;
        private final ZonedDateTime roundedTimeStamp;

        public AuxDataCacheKey(AuxiliaryServiceName service, ZonedDateTime timeStamp) {
            this.service = service;
            this.roundedTimeStamp = floorToQuarterHour(timeStamp);
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AuxDataCacheKey cacheKey = (AuxDataCacheKey) o;

            if (!roundedTimeStamp.equals(cacheKey.roundedTimeStamp)) return false;

            return service == cacheKey.service;

        }

        @Override
        public int hashCode() {
            int result = service.hashCode();
            result = 31 * result + roundedTimeStamp.hashCode();
            return result;
        }
    }


    /**
     * public for unit testing purposes
     *
     * @param service the name of the aux data to fetch
     * @param time    the time stamp of the data event
     * @return the TreeSet containing the AuxPoints
     */
    public TreeSet<AuxPoint> loadDataFromDataBase(AuxiliaryServiceName service, ZonedDateTime time) {
        TreeSet<AuxPoint> result = new TreeSet<>();

        try {
//            log.info("Querying Database for " + service.toString() + " data for time " + time.toString());

            SqlJetDb db = SqlJetDb.open(new File(url.getFile()), true);
            ISqlJetTable table = db.getTable(service.toString());
            ISqlJetCursor cursor = null;
            try {

                db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
                try {
                    if (service == AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION) {


                        String earlierTime = time.minusHours(4).toString();
                        String[] dateAndTime = earlierTime.split("T");
                        String[] timeSplitZone = dateAndTime[1].split("Z");
                        String earlier = dateAndTime[0] + " " + timeSplitZone[0].substring(0, 8);

                        String laterTime = time.plusMinutes(window).toString();
                        dateAndTime = laterTime.split("T");
                        timeSplitZone = dateAndTime[1].split("Z");
                        String later = dateAndTime[0] + " " + timeSplitZone[0].substring(0, 8);

                        cursor = table.scope("ix_DRIVE_CONTROL_TRACKING_POSITION_Time", new Object[]{earlier}, new Object[]{later});
                        result = getTrackingDataFromCursor(cursor);
                    } else if (service == AuxiliaryServiceName.DRIVE_CONTROL_SOURCE_POSITION) {
                        //source position is slower. get data from several hours ago.
                        String earlierTime = time.minusHours(4).toString();
                        String[] dateAndTime = earlierTime.split("T");
                        String[] timeSplitZone = dateAndTime[1].split("Z");
                        String earlier = dateAndTime[0] + " " + timeSplitZone[0].substring(0, 8);

                        String laterTime = time.plusMinutes(window).toString();
                        dateAndTime = laterTime.split("T");
                        timeSplitZone = dateAndTime[1].split("Z");
                        String later = dateAndTime[0] + " " + timeSplitZone[0].substring(0, 8);

                        cursor = table.scope("ix_DRIVE_CONTROL_SOURCE_POSITION_Time", new Object[]{earlier}, new Object[]{later});
                        result = getSourceDataFromCursor(cursor);
                    } else {
                        throw new RuntimeException("This service only provides data from DRIVE_CONTROL_SOURCE_POSITION  and TRACKING files");
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

            } finally {
                db.commit();
                db.close();
            }

        } catch (SqlJetException e) {
            e.printStackTrace();
        }
        return result;
    }

    private TreeSet<AuxPoint> getSourceDataFromCursor(ISqlJetCursor cursor) throws SqlJetException {
        TreeSet<AuxPoint> result = new TreeSet<>();
        if (!cursor.eof()) {
            do {
                Map<String, Serializable> m = new HashMap<>();
                m.put("QoS", cursor.getInteger("QoS"));
                //getFloat actually returns a double. WTF
                m.put("Name", cursor.getString("Name"));
                m.put("Angle", cursor.getFloat("Angle"));
                m.put("Ra_src", cursor.getFloat("Ra_src"));
                m.put("Dec_src", cursor.getFloat("Dec_src"));
                // m.put("Ra_cmd", cursor.getFloat("Ra_cmd"));
                // m.put("Dec_cmd", cursor.getFloat("Dec_cmd"));
                m.put("Offset", cursor.getFloat("Offset"));

                String tempTime = cursor.getString("Time");
                tempTime = tempTime.concat("+00:00");
                tempTime = tempTime.replace(" ", "T");
                ZonedDateTime t = ZonedDateTime.parse(tempTime);
                result.add(new AuxPoint(t, m));
            } while (cursor.next());
        }
        return result;
    }

    private TreeSet<AuxPoint> getTrackingDataFromCursor(ISqlJetCursor cursor) throws SqlJetException {
        TreeSet<AuxPoint> result = new TreeSet<>();
        if (!cursor.eof()) {
            do {
                Map<String, Serializable> m = new HashMap<>();
                m.put("QoS", cursor.getInteger("QoS"));
                //getFloat actually returns a double. WTF
                m.put("Zd", cursor.getFloat("Zd"));
                m.put("Az", cursor.getFloat("Az"));
                m.put("Ra", cursor.getFloat("Ra"));
                m.put("Dec", cursor.getFloat("Dec"));
                m.put("dZd", cursor.getFloat("dZd"));
                m.put("dAz", cursor.getFloat("dAz"));
                m.put("dev", cursor.getFloat("dev"));


                String tempTime = cursor.getString("Time");
                tempTime = tempTime.concat("+00:00");
                tempTime = tempTime.replace(" ", "T");


                ZonedDateTime t = ZonedDateTime.parse(tempTime);

                result.add(new AuxPoint(t, m));
            } while (cursor.next());
        }
        return result;
    }


    /**
     * Get auxiliary data from the cache connected to the sqlite database holding drive information.
     *
     * @param service        The service to query.
     * @param eventTimeStamp The timestamp of your current event.
     * @return the data closest to the eventtimestamp which is found in the database according to the strategy.
     */
    @Override
    public AuxPoint getAuxiliaryData(AuxiliaryServiceName service, ZonedDateTime eventTimeStamp, AuxPointStrategy strategy) throws IOException {
        try {
            AuxDataCacheKey key = new AuxDataCacheKey(service, eventTimeStamp);
            //this set might not contain the data we need
            TreeSet<AuxPoint> set = cache.get(key);
            AuxPoint a = strategy.getPointFromTreeSet(set, eventTimeStamp);
            if (a == null) {
                throw new IOException("No auxpoint found for the given timestamp " + eventTimeStamp);
            }
            TimeUnit tu = TimeUnit.SECONDS;
            long difference = eventTimeStamp.toEpochSecond() - a.getTimeStamp().toEpochSecond();
            long seconds = tu.toSeconds(difference);
            log.debug("Seconds between event and {} auxpoint : {}", service, seconds);
            return strategy.getPointFromTreeSet(set, eventTimeStamp);

        } catch (ExecutionException e) {
            throw new IOException("Could not load data from Database. Is the database readable?");
//            e.printStackTrace();
        }
    }


    @Override
    public void reset() throws Exception {
        log.info("Reset called on {}", this.getClass());
    }

    public void setUrl(SourceURL url) {
        this.url = url;
    }

    public void setWindow(int window) {
        this.window = window;
    }


}
