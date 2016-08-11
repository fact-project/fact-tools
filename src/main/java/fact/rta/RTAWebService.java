package fact.rta;

import com.google.common.collect.Range;
import com.google.gson.*;
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
import stream.Data;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import stream.service.Service;
import streams.runtime.Signals;

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

    private DBI dbi;

    final private static Logger log = LoggerFactory.getLogger(RTAWebService.class);


    final private Gson gson;
    private boolean isInit = false;

    private RTADataBase.FACTRun currentRun = null;

    final private class RTAEvent {
        final double[] photonCharges;
        final double estimatedEnergy;
        final double size;
        final double thetaSquare;
        final String sourceName;
        final DateTime eventTimeStamp;

        RTAEvent(DateTime eventTimeStamp, Data item) {
            this.thetaSquare = (double) item.get("signal:thetasquare");
            this.estimatedEnergy = (double) item.get("energy");
            this.size = (double) item.get("Size");
            this.sourceName = (String) item.get("SourceName");
            this.photonCharges = (double[]) item.get("photoncharge");
            this.eventTimeStamp = eventTimeStamp;
        }
    }

    final private class RTADataRate {
        final double rate;
        final DateTime date;

        RTADataRate(DateTime date, double rate) {
            this.rate = rate;
            this.date = date;
        }
    }


    private TreeMap<DateTime, RTAEvent> eventMap = new TreeMap<>();
    private TreeMap<DateTime, Double> rateMap = new TreeMap<>();
    private TreeMap<DateTime, StatusContainer> systemStatusMap = new TreeMap<>();
    private TreeMap<DateTime, RTADataBase.RTASignal> signalMap = new TreeMap<>();

    public RTAWebService() throws SQLException {

        gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(Range.class, new RangeSerializer())
                .registerTypeAdapter(DateTime.class, new DateTimeAdapter())
                .create();

        Spark.staticFileLocation("/rta");
        Spark.get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("title", "FACT RTA - Real Time Analysis");
            return new ModelAndView(attributes, "rta/index.html");
        }, new HandlebarsTemplateEngine());

//        Spark.get("/lightcurve", (request, response) -> getLightCurve(request.queryParams("hours")), gson::toJson);

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
        long MINUTE = 1000*60;

        final Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                systemStatusMap.put(DateTime.now(), StatusContainer.create());
                Minutes deltaT = Minutes.minutesBetween(systemStatusMap.firstKey(), systemStatusMap.lastKey());
                if(deltaT.isGreaterThan(Minutes.minutes(30))){
                    systemStatusMap.pollFirstEntry();
                }
            }
        }, 0, (long) (0.1*MINUTE));

        Signals.register(i -> t.cancel());
    }

    public void init(){
        dbi = new DBI("jdbc:sqlite:" + sqlitePath.getPath());
        RTADataBase.DBInterface rtaDBInterface = dbi.open(RTADataBase.DBInterface.class);
        rtaDBInterface.createRunTable();
        rtaDBInterface.createSignalTable();
        isInit = true;
    }
//    private int background(Data data, double thetaCut){
//        int backGroundEvents = 0;
//
//        Keys offKeys = new Keys("Theta_Off_?");
//        for (String key : offKeys.select(data)) {
//            double offValue = (double) data.get(key);
//            if (offValue > thetaCut){
//                backGroundEvents += 1;
//            }
//        }
//        return backGroundEvents;
//    }
//
//    private int signal(Data data, double predictionThreshold, double thetaCut){
//        double prediction = (double) data.get("signal:prediction");
//        double theta = (double) data.get("signal:thetasquare");
//        if (prediction <= predictionThreshold && theta <= thetaCut){
//            return 1;
//        }
//        return 0;
//    }

    ArrayList<Double> relativeOnTimes = new ArrayList<>();
    synchronized void updateEvent(DateTime eventTimeStamp, Data item, double relativeOnTime){

        if (!isInit){
            init();
        }



        RTADataBase.FACTRun run = new RTADataBase().new FACTRun(item);
        if (currentRun == null){
            RTADataBase.DBInterface rtaTables = this.dbi.open(RTADataBase.DBInterface.class);
            rtaTables.insertRun(run);
            currentRun = run;
        }
        else if (!currentRun.equals(run)){
            RTADataBase.DBInterface rtaTables = this.dbi.open(RTADataBase.DBInterface.class);
            double average = relativeOnTimes.stream().mapToDouble(a -> a).average().orElse(0);
            log.info("New run found. Relative on time of previous run was {}", average);
            rtaTables.updateRunWithOnTime(average, currentRun.runID, currentRun.night);
            if (average > 0){
                log.info("Setting status of previous run to {}" , RTADataBase.HEALTH.OK);
                rtaTables.updateRunHealth(RTADataBase.HEALTH.OK, currentRun.runID, currentRun.night);
            } else {
                log.warn("RelativeOnTime of previous run was 0. Marking run status as UNKNOWN");
                rtaTables.updateRunHealth(RTADataBase.HEALTH.UNKNOWN, currentRun.runID, currentRun.night);
            }
            //insert new run to db
            rtaTables.insertRun(run);
            currentRun = run;
            relativeOnTimes.clear();
        }
        relativeOnTimes.add(relativeOnTime);

        signalMap.put(DateTime.now(), new RTADataBase().new RTASignal(eventTimeStamp, item, run));
        Seconds delta = Seconds.secondsBetween(signalMap.firstKey(), signalMap.lastKey());
        if(delta.isGreaterThan(Seconds.seconds(10))){
            persistEvents(signalMap);
        }


        eventMap.put(DateTime.now(), new RTAEvent(eventTimeStamp, item));
        delta = Seconds.secondsBetween(eventMap.firstKey(), eventMap.lastKey());
        if(delta.isGreaterThan(Seconds.seconds(10))){
            eventMap.pollFirstEntry();
        }
    }

    private void persistEvents(TreeMap<DateTime, RTADataBase.RTASignal> signalMap) {
        log.info("Saving stuff to DB");
        if (!isInit){
            init();
        }
        RTADataBase.DBInterface rtaTables = this.dbi.open(RTADataBase.DBInterface.class);
        while(!signalMap.isEmpty()){
            RTADataBase.RTASignal rtaSignal = signalMap.pollFirstEntry().getValue();
            rtaTables.insertRun(rtaSignal.run);
            rtaTables.insertSignal(rtaSignal);
        }
        rtaTables.close();
    }

    private RTAEvent getLatestEvent(){
        if (!eventMap.isEmpty()) {
            return eventMap.lastEntry().getValue();
        }
        return null;
    }

    private ArrayList<RTADataRate> getDataRates(String timeStamp){
        if (rateMap.isEmpty()) {
            return null;
        }

        NavigableMap<DateTime, Double> resultMap;
        if (timeStamp != null) {
            resultMap = rateMap.tailMap(DateTime.parse(timeStamp), false).descendingMap();
        }
        else {
            resultMap = rateMap.descendingMap();
        }

        ArrayList<RTADataRate> rates = new ArrayList<>();
        resultMap.forEach((k, v) ->{
            rates.add(new RTADataRate(k, v));
        });
        return rates;
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

//
//    private Map<Range<DateTime>, RTAProcessor.SignalContainer> getLightCurve(String minusHours) throws NumberFormatException{
//        RTADataBase.DBInterface rtaTables = this.dbi.open(RTADataBase.DBInterface.class);
//        rtaTables.
//        if(lightCurve.asMapOfRanges().isEmpty()){
//            return null;
//        }
//
//        if(minusHours != null){
//            Integer hours = Integer.parseInt(minusHours);
//            DateTime history = DateTime.now().minusHours(hours);
//
//            Map<Range<DateTime>, RTAProcessor.SignalContainer> resultMap = new HashMap<>();
//
//            lightCurve.asDescendingMapOfRanges().forEach((range, container) ->{
//                if(range.lowerEndpoint().isAfter(history)){
//                    resultMap.put(range, container);
//                }
//            });
//
//            return resultMap;
//        }
//        return lightCurve.asDescendingMapOfRanges();
//    }

    @Override
    public void reset() throws Exception {

    }



    private static class DateTimeAdapter extends TypeAdapter<DateTime> {

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
