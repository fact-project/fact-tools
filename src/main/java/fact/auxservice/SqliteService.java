package fact.auxservice;

import com.google.common.cache.*;
import com.google.gson.*;
import fact.auxservice.strategies.AuxPointStrategy;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.web.Resty;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * This implements an AuxiliaryService {@link AuxiliaryService}  providing data from some
 *
 *
 * Created by kai on 01.03.15.
 */
public class SqliteService implements AuxiliaryService {

    static Logger log = LoggerFactory.getLogger(SqliteService.class);
    final static String INDEX = "ix_DRIVE_CONTROL_SOURCE_POSITION_Time";

    @Parameter(required = true, description = "The URL to the sqlite database file. ")
    private SourceURL url;

    @Parameter(required = false, description = "Timewindow for which to query the Database.", defaultValue = "20 Minutes")
    private int window = 20;

    private LoadingCache<CacheKey, TreeSet<AuxPoint>> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    log.info("Removing Data {} from cache for cause {}", notification.toString() ,notification.getCause());
                }
            })
            .build(new CacheLoader<CacheKey, TreeSet<AuxPoint>>() {
                @Override
                public TreeSet<AuxPoint> load(CacheKey key) throws Exception {
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
            if (service != cacheKey.service) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = service.hashCode();
            result = 31 * result + roundedTimeStamp.hashCode();
            return result;
        }
    }


    private static void printRecords(ISqlJetCursor cursor) throws SqlJetException {
        try {
            if (!cursor.eof()) {
                do {
                    System.out.println(cursor.getRowId() + " : " +
                            cursor.getString("Dec") + " " +
                            cursor.getString("Ra") + "  " +
                            new DateTime(cursor.getInteger("Time")));
                } while(cursor.next());
            }
        } finally {
            cursor.close();
        }
    }

    public TreeSet<AuxPoint> loadDataFromDataBase(AuxiliaryServiceName service, DateTime time){
        try {
            log.info("Querying Database for " + service.toString() + " data for time " + time.toString());
            TreeSet<AuxPoint> result = new TreeSet<>();

            SqlJetDb db = SqlJetDb.open(new File(url.getFile()), true);
            ISqlJetTable table = db.getTable(service.toString());

            try {
                db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
                System.out.println(time.minusMinutes(122).getMillis()/1000);
                ISqlJetCursor r = table.scope(INDEX,
                        new Object[]{Integer.MIN_VALUE},
                        new Object[]{Integer.MAX_VALUE});

                printRecords(r);


            } finally {
                db.commit();
                db.close();
            }



        } catch (SqlJetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param service The service to query.
     * @param eventTimeStamp The timestamp of your current event.
     * @return the data closest to the eventtimestamp which is found in the database.
     */
    public AuxPoint getAuxiliaryData(AuxiliaryServiceName service, DateTime eventTimeStamp, AuxPointStrategy strategy) throws IOException {
        try {
            CacheKey key = new CacheKey(service, eventTimeStamp);
            //this set might not contain the data we need
            TreeSet<AuxPoint> set = cache.get(key);
            return strategy.getPointFromTreeSet(set, eventTimeStamp);

        } catch (ExecutionException e) {
            throw new IOException("Could not load data from Database. Are you connected to the intertubes?");
//            e.printStackTrace();
        }
    }


    public static DateTime floorToQuarterHour(DateTime time){
        DateTime t = time.secondOfMinute().setCopy(0);
        int oldMinute = t.getMinuteOfHour();
        int newMinute = 15 * (int) Math.floor(oldMinute / 15.0);
        return t.plusMinutes(newMinute - oldMinute);
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
