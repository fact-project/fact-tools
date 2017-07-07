package fact.auxservice;

import com.google.common.cache.*;
import com.google.gson.*;
import fact.auxservice.strategies.AuxPointStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.annotations.Parameter;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.web.Resty;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * This implements an AuxiliaryService {@link fact.auxservice.AuxiliaryService}  providing data from some webservice
 *
 * TODO: get the webservice online.
 *
 * Created by kai on 01.03.15.
 */
public class AuxWebService implements AuxiliaryService {

    static Logger log = LoggerFactory.getLogger(AuxWebService.class);
    Resty r = new Resty();
    Gson gson = new GsonBuilder().registerTypeAdapter(AuxPoint.class, new AuxPointDeserializer()).create();

    @Parameter(required = true, description = "The URL to the webservice. No trailing slashes.")
    private String url = "http://127.0.0.1:5000";

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
        private ZonedDateTime roundedTimeStamp;

        public CacheKey(AuxiliaryServiceName service, ZonedDateTime timeStamp) {
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
    private class AuxPointDeserializer implements JsonDeserializer<AuxPoint> {
        public AuxPoint deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException {

            long value = (long) (((JsonObject) json).get("Time").getAsDouble() * 1000 * 24 * 3600);

            Instant insMill = Instant.ofEpochMilli(value);
            ZonedDateTime timeStamp = ZonedDateTime.ofInstant(insMill, ZoneOffset.UTC);

            Map<String, Serializable> data = new HashMap<>();
            data = new Gson().fromJson(json, data.getClass());
            return new AuxPoint(timeStamp, data);
        }
    }



    private TreeSet<AuxPoint> loadDataFromDataBase(AuxiliaryServiceName service, ZonedDateTime time){
        try {
            log.info("Querying Database for " + service.toString() + " data for time " + time.toString());
            TreeSet<AuxPoint> result = new TreeSet<>();

            JSONArray json = r.json(url + "/aux/" + service.toString() +"?from="+ time.minusMinutes(window) + "&to=" + time.plusMinutes(window)).array();
            for (int i = 0; i < json.length(); i++) {
                result.add(gson.fromJson(json.getString(i), AuxPoint.class));
            }
            return result;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param service The service to query.
     * @param eventTimeStamp The timestamp of your current event.
     * @return the data closest to the eventtimestamp which is found in the database.
     */
    public AuxPoint getAuxiliaryData(AuxiliaryServiceName service, ZonedDateTime eventTimeStamp, AuxPointStrategy strategy) throws IOException {
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


    public static ZonedDateTime floorToQuarterHour(ZonedDateTime time){
        ZonedDateTime t = time.withZoneSameInstant(ZoneOffset.UTC).withSecond(0);
        int oldMinute = t.getMinute();
        int newMinute = 15 * (int) Math.floor(oldMinute / 15.0);
        return t.plusMinutes(newMinute - oldMinute);
    }

    @Override
    public void reset() throws Exception {

    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setWindow(int window) {
        this.window = window;
    }


}
