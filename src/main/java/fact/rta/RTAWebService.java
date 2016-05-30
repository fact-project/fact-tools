package fact.rta;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import stream.service.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;


/**
 * Created by kai on 24.01.16.
 */
public class RTAWebService implements Service {


    @Parameter(required = true, description = "Path to the .sqlite file")
    SourceURL sqlitePath;



    private final DBI dbi;

    final private static Logger log = LoggerFactory.getLogger(RTAWebService.class);


    final private Gson gson;


    final private class RTAEvent{
        final double[] photonCharges;
        final double estimatedEnergy;
        final double size;
        final double thetaSquare;
        final String sourceName;
        final DateTime eventTimeStamp;

        RTAEvent(double[] photonCharges, double estimatedEnergy, double size, double thetaSquare, String sourceName, DateTime eventTimeStamp){

            this.photonCharges = photonCharges;
            this.estimatedEnergy = estimatedEnergy;
            this.size = size;
            this.thetaSquare = thetaSquare;
            this.sourceName = sourceName;
            this.eventTimeStamp = eventTimeStamp;
        }
    }


    private TreeMap<DateTime, RTAEvent> eventMap = new TreeMap<>();
    private TreeMap<DateTime, Double> rateMap = new TreeMap<>();
    private TreeMap<DateTime, StatusContainer> systemStatusMap = new TreeMap<>();
    private TreeRangeMap<DateTime, RTAProcessor.SignalContainer> lightCurve = TreeRangeMap.create();

    public RTAWebService(String path) throws SQLException {
        Type type = new TypeToken<Range<DateTime>>() {}.getType();

        gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(Range.class, new RangeSerializer())
                .registerTypeAdapter(DateTime.class, new DateTimeAdapter())
                .create();
        //fuck this
        if (sqlitePath != null) {
            dbi = new DBI("jdbc:sqlite:" + sqlitePath.getPath());
        } else {
            dbi = new DBI("jdbc:sqlite:" + path);
        }


        Spark.staticFileLocation("/rta");
        Spark.get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("title", "FACT RTA - Real Time Analysis");
            return new ModelAndView(attributes, "rta/index.html");
        }, new HandlebarsTemplateEngine());

        Spark.get("/lightcurve", (request, response) -> getLightCurve(request.queryParams("hours")), gson::toJson);

        Spark.get("/datarate",  (request, response) -> getDataRates(request.queryParams("timestamp")), gson::toJson);

        Spark.get("/event", (request, response) -> getLatestEvent(), gson::toJson);

        Spark.get("/status",  (request, response) -> getSystemStatus(request.queryParams("timestamp")), gson::toJson);

        Spark.exception(NumberFormatException.class, (e, req, res) ->{
            res.status(400);
            res.body(gson.toJson("Error. Bad Request. Cannot parse number " +req.queryParams("hours") +   " in query string"));
        });

        Spark.exception(IllegalArgumentException.class, (e, req, res) ->{
            res.status(400);
            res.body(gson.toJson("Error. Bad Request. Cannot parse date " +req.queryParams("timestamp") +  " in query string"));
        });


        //update systemstatus once per minute
        int MINUTE = 1000*60;

        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                systemStatusMap.put(DateTime.now(), StatusContainer.create());
                Minutes deltaT = Minutes.minutesBetween(systemStatusMap.firstKey(), systemStatusMap.lastKey());
                if(deltaT.isGreaterThan(Minutes.minutes(30))){
                    systemStatusMap.pollFirstEntry();
                }
            }
        }, 0, 10000);

    }


    public void updateEvent(double[] photoncharges,double  estimatedEnergy, double size, double thetaSquare, String sourceName, DateTime eventTimeStamp){
        RTAEvent event = new RTAEvent(photoncharges, estimatedEnergy, size, thetaSquare, sourceName, eventTimeStamp);
        DateTime now = DateTime.now();
        eventMap.put(now, event);

        Seconds delta = Seconds.secondsBetween(eventMap.firstKey(), eventMap.lastKey());
        if(delta.isGreaterThan(Seconds.seconds(180))){
            rateMap.pollFirstEntry();
        }
    }

    public RTAEvent getLatestEvent(){
        if (!eventMap.isEmpty()) {
            return eventMap.lastEntry().getValue();
        }
        return null;
    }

    public NavigableMap<DateTime, Double> getDataRates(String timeStamp){
        if (rateMap.isEmpty()){
            return null;
        }
        if (timeStamp != null) {
            return rateMap.tailMap(DateTime.parse(timeStamp), false);
        }
        return rateMap.descendingMap();
    }

    public void updateDataRate(DateTime timeStamp, Double dataRate){
        rateMap.put(timeStamp, dataRate);
        Seconds delta = Seconds.secondsBetween(rateMap.firstKey(), timeStamp);
        if(delta.isGreaterThan(Seconds.seconds(180))){
            rateMap.pollFirstEntry();
        }
    }



    private NavigableMap<DateTime, StatusContainer> getSystemStatus(String timeStamp){
        if(systemStatusMap.isEmpty()){
            return null;
        }
        if (timeStamp != null) {
            return systemStatusMap.tailMap(DateTime.parse(timeStamp), false);
        }
        return systemStatusMap.descendingMap();
    }


    private Map<Range<DateTime>, RTAProcessor.SignalContainer> getLightCurve(String minusHours) throws NumberFormatException{
        if(lightCurve.asMapOfRanges().isEmpty()){
            return null;
        }

        if(minusHours != null){
            Integer hours = Integer.parseInt(minusHours);
            DateTime history = DateTime.now().minusSeconds(hours);

            Map<Range<DateTime>, RTAProcessor.SignalContainer> resultMap = new HashMap<>();

            lightCurve.asDescendingMapOfRanges().forEach((range, container) ->{
                if(range.lowerEndpoint().isAfter(history)){
                    resultMap.put(range, container);
                }
            });

            return resultMap;
        }
        return lightCurve.asDescendingMapOfRanges();
    }

    @Override
    public void reset() throws Exception {

    }


    public void updateLightCurve(Range<DateTime> edgesOfCurrentBin, RTAProcessor.SignalContainer signalContainer, String source) {
        lightCurve.put(edgesOfCurrentBin, signalContainer);
        // remove old bins so we don't accumulate too many old results in memory

        Map.Entry<Range<DateTime>, RTAProcessor.SignalContainer> entry = lightCurve.getEntry(edgesOfCurrentBin.lowerEndpoint().minusHours(12));
        if (entry != null){
            lightCurve.remove(entry.getKey());
        }
        persist(lightCurve, source);
    }

    private void persist(TreeRangeMap<DateTime, RTAProcessor.SignalContainer> lightCurve, String source) {
        //its probably enough to just insert the lates entry here. I think. Anyways lets try and insert all of them.
        RTASignalTable t = dbi.open(RTASignalTable.class);
        lightCurve.asDescendingMapOfRanges().forEach((dateTimeRange, signalContainer) -> {

            DateTime start = dateTimeRange.lowerEndpoint();
            DateTime end = dateTimeRange.upperEndpoint();
            t.insertOrIgnore(start.toString(), end.toString(), source, signalContainer.signalEvents, signalContainer.backgroundEvents);
        });
    }

    public static class DateTimeAdapter extends TypeAdapter<DateTime> {

        @Override
        public void write(JsonWriter jsonWriter, DateTime dateTime) throws IOException {
            if (dateTime == null){
                jsonWriter.nullValue();
            }
            else{
                jsonWriter.value(dateTime.toString("y-M-d'T'H:mm:s.SSS"));
            }
        }

        @Override
        public DateTime read(JsonReader jsonReader) throws IOException {
            return DateTime.parse(jsonReader.toString());
        }
    }

    private class RangeSerializer implements JsonSerializer<Range<DateTime>> {
        public JsonElement serialize(Range<DateTime> range, Type typeOfSrc, JsonSerializationContext context) {
            String format = "y-M-d'T'H:mm:s.SSS";
            JsonObject obj = new JsonObject();
            obj.addProperty("start", range.lowerEndpoint().toString(format));
            obj.addProperty("end", range.upperEndpoint().toString(format));
            return obj;
        }
    }
}
