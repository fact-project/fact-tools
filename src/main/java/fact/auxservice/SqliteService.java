package fact.auxservice;

import com.google.common.cache.*;
import fact.auxservice.strategies.AuxPointStrategy;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.*;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * This implements an AuxiliaryService {@link AuxiliaryService}  providing data from a sqlite file.
 * Can handle TRACKING and SOURCE information.
 *
 * Created by kai on 01.03.15.
 */
public class SqliteService implements AuxiliaryService {

    static Logger log = LoggerFactory.getLogger(SqliteService.class);

    @Parameter(required = true, description = "The URL to the sqlite database file. ")
    private SourceURL url;

    @Parameter(required = false, description = "Expiry time for entries in the cache.", defaultValue = "10 Minutes")
    private int window = 10;

    private LoadingCache<CacheKey, TreeSet<AuxPoint>> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(window, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    log.info("Removing Data {} from cache for cause {}", notification.toString() ,notification.getCause());
                }
            })
            .build(new CacheLoader<CacheKey, TreeSet<AuxPoint>>() {
                @Override
                public TreeSet<AuxPoint> load(CacheKey key) throws Exception {
                    log.info("Building entry for service {} and key: {}", key.service, key.roundedTimeStamp);
                    return loadDataFromDataBase(key.service, key.roundedTimeStamp);
                }
            });

    private class CacheKey{
        private AuxiliaryServiceName service;
        private DateTime roundedTimeStamp;

        public CacheKey(AuxiliaryServiceName service, DateTime timeStamp) {
            this.service = service;
            this.roundedTimeStamp = floorToQuarterHour(timeStamp);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

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
     * @param service the name of the aux data to fetch
     * @param time the time stamp of the data event
     * @return the TreeSet containing the AuxPoints
     */
    public TreeSet<AuxPoint> loadDataFromDataBase(AuxiliaryServiceName service, DateTime time){
        TreeSet<AuxPoint> result = new TreeSet<>();

        try {
//            log.info("Querying Database for " + service.toString() + " data for time " + time.toString());

            SqlJetDb db = SqlJetDb.open(new File(url.getFile()), true);
            ISqlJetTable table = db.getTable(service.toString());
            ISqlJetCursor cursor = null;
            try {

                db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
                try {
                    if(service == AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION){
                        String earlier = time.minusHours(4).toString("YYYY-MM-DD HH:mm:ss");
                        String later = time.plusMinutes(window).toString("YYYY-MM-DD HH:mm:ss");

                        cursor = table.scope("ix_DRIVE_CONTROL_TRACKING_POSITION_Time", new Object[]{earlier}, new Object[]{later});
                        result = getTrackingDataFromCursor(cursor);
                    } else if(service == AuxiliaryServiceName.DRIVE_CONTROL_SOURCE_POSITION){
                        //source position is slower. get data from several hours ago.
                        String earlier = time.minusHours(4).toString("YYYY-MM-DD HH:mm:ss");
                        String later = time.plusMinutes(window).toString("YYYY-MM-DD HH:mm:ss");

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
                m.put("Ra_cmd", cursor.getFloat("Ra_cmd"));
                m.put("Dec_cmd", cursor.getFloat("Dec_cmd"));
                m.put("Offset", cursor.getFloat("Offset"));

                DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-DD HH:mm:ss.SSSSSS");
                DateTime t = DateTime.parse(cursor.getString("Time"), formatter).withZone(DateTimeZone.UTC);

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

                DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-DD HH:mm:ss.SSSSSS");
                DateTime t = DateTime.parse(cursor.getString("Time"), formatter).withZone(DateTimeZone.UTC);

                result.add(new AuxPoint(t, m));
            } while (cursor.next());
        }
        return result;
    }


    /**
     * Get auxiliary data from the cache connected to the sqlite database holding drive information.
     * @param service The service to query.
     * @param eventTimeStamp The timestamp of your current event.
     * @return the data closest to the eventtimestamp which is found in the database according to the strategy.
     */
    @Override
    public AuxPoint getAuxiliaryData(AuxiliaryServiceName service, DateTime eventTimeStamp, AuxPointStrategy strategy) throws IOException {
        try {
            CacheKey key = new CacheKey(service, eventTimeStamp);
            //this set might not contain the data we need
            TreeSet<AuxPoint> set = cache.get(key);
            AuxPoint a = strategy.getPointFromTreeSet(set, eventTimeStamp);
            Seconds seconds = Seconds.secondsBetween(eventTimeStamp, a.getTimeStamp());
            log.debug("Seconds between event and {} auxpoint : {}", service,  seconds.getSeconds());
            return strategy.getPointFromTreeSet(set, eventTimeStamp);

        } catch (ExecutionException e) {
            throw new IOException("Could not load data from Database. Is the database readable?");
//            e.printStackTrace();
        }
    }


    public static DateTime floorToQuarterHour(DateTime time){

        MutableDateTime t = time.toMutableDateTime();
        t.setMillisOfSecond(0);
        t.setSecondOfMinute(0);

        int oldMinute = t.getMinuteOfHour();
        int newMinute = 15 * (int) Math.floor(oldMinute / 15.0);
        t.setMinuteOfHour(newMinute);
        return new DateTime(t);
    }

    @Override
    public void reset() throws Exception {

    }

    public void setUrl(SourceURL url) {
        this.url = url;
    }

    public void setWindow(int window) {
        this.window = window;
    }


}
