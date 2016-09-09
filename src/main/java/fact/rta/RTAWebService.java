package fact.rta;

import com.google.common.collect.Range;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fact.rta.db.FACTRun;
import fact.rta.rest.LightCurveBin;
import fact.rta.rest.RTASignal;
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
import java.util.stream.Stream;


/**
 *
 * TODO: use priority heap for multithreading. or add some way to know when the run has been completly processed.
 *
 * The Signal and DataRate Processors update stuff in ths service.
 * Created by kai on 24.01.16.
 */
public class RTAWebService implements Service {


    @Parameter(required = true, description = "Path to the .sqlite file")
    SourceURL sqlitePath;

    private DBI dbi;

    final private static Logger log = LoggerFactory.getLogger(RTAWebService.class);


    final private Gson gson;
    private boolean isInit = false;

    private FACTRun currentRun = null;

    /**
     * This will be propagated to the frontend
     */
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

    /**
     * datarate container for the frontend
     */
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

    private ArrayList<RTASignal> rtaSignals = new ArrayList<>();

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
            return new ModelAndView(attributes, "index.html");
        }, new HandlebarsTemplateEngine("/rta"));

        Spark.get("/lightcurve", (request, response) -> {
            DateTime start = DateTime.parse(request.queryParams("start"));
            DateTime end = DateTime.parse(request.queryParams("end"));
            Integer binning = Integer.parseInt(request.queryParams("binning"));

            return getLightCurve(start, end, binning);
        }, gson::toJson);

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


    synchronized void updateEvent(DateTime eventTimeStamp, Data item, double relativeOnTime){

        if (!isInit){
            init();
        }


        FACTRun run = new FACTRun(item);
        if (currentRun == null){
            RTADataBase.DBInterface rtaTables = this.dbi.open(RTADataBase.DBInterface.class);
            rtaTables.insertRun(run);
            currentRun = run;
        }
        else if (!currentRun.equals(run)){
            log.info("New run found. OnTime of new run is: {} seconds.", run.onTime.getStandardSeconds());
            //save signals to database
            persistEvents(rtaSignals, currentRun);

            RTADataBase.DBInterface rtaTables = this.dbi.open(RTADataBase.DBInterface.class);
            rtaTables.updateRunHealth(RTADataBase.HEALTH.OK, currentRun.runID, currentRun.night);

            //insert new run to db
            rtaTables.insertRun(run);
            currentRun = run;

        }

        rtaSignals.add(new RTASignal(eventTimeStamp, DateTime.now(), item, currentRun));
        eventMap.put(DateTime.now(), new RTAEvent(eventTimeStamp, item));


        Seconds delta = Seconds.secondsBetween(eventMap.firstKey(), eventMap.lastKey());
        if(delta.isGreaterThan(Seconds.seconds(60))){
            eventMap.pollFirstEntry();
        }
    }

    private void persistEvents(ArrayList<RTASignal> signals, FACTRun run) {
        log.info("Saving stuff to DB");
        if (!isInit){
            init();
        }

        double onTimePerEvent = run.onTime.getStandardSeconds()/signals.size();
        RTADataBase.DBInterface rtaTables = this.dbi.open(RTADataBase.DBInterface.class);

        signals.forEach(signal -> {
                    signal.onTimePerEvent = onTimePerEvent;
                    rtaTables.insertSignal(signal);
                });
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



    private List<LightCurveBin> getLightCurve(DateTime startTime, DateTime endTime, int binningInMinutes) throws NumberFormatException{
        double alpha = 0.2;
        double predictionThreshold = 0.95;
        double thetaThreshold = 0.04;

        ArrayList<LightCurveBin> lc = new ArrayList<>();

        RTADataBase.DBInterface rtaTables = this.dbi.open(RTADataBase.DBInterface.class);
        final List<RTASignal> signalEntries = rtaTables.getSignalEntries(startTime.toString("YYYY-MM-dd HH:mm:ss"), endTime.toString("YYYY-MM-dd HH:mm:ss"));

        //fill a treemap
        TreeMap<DateTime, RTASignal> dateTimeRTASignalTreeMap = new TreeMap<>();
        signalEntries.forEach(a -> dateTimeRTASignalTreeMap.put(a.eventTimestamp, a));

        //iterate over all the bins
        for (int bin = 0; bin < binningInMinutes; bin++) {
            //get all entries in bin
            SortedMap<DateTime, RTASignal> subMap = dateTimeRTASignalTreeMap.subMap(startTime, startTime.plusMinutes(bin));
            Stream<RTASignal> rtaSignalStream = subMap.entrySet().stream().map(Map.Entry::getValue);

            //get on time in this bin by summing the ontimes per event in each row
            double onTimeInBin = rtaSignalStream.mapToDouble(a -> a.onTimePerEvent).sum();

            //select gamma like events and seperate signal and backgorund region
            Stream<RTASignal> gammaLike = rtaSignalStream.filter(s -> s.prediction > predictionThreshold);

            int signal = (int) gammaLike
                    .filter(s -> s.theta < thetaThreshold)
                    .count();

            int background = (int) gammaLike
                    .filter(s-> (
                            s.theta_off_1 < thetaThreshold ||
                            s.theta_off_2 < thetaThreshold ||
                            s.theta_off_3 < thetaThreshold ||
                            s.theta_off_4 < thetaThreshold ||
                            s.theta_off_5 < thetaThreshold
                    ))
                    .count();
            LightCurveBin lightCurveBin = new LightCurveBin(startTime, startTime.plusMinutes(bin), background, signal, alpha, onTimeInBin);
            lc.add(lightCurveBin);

        }
        return lc;
    }

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
