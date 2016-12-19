package fact.rta;

import com.google.common.collect.Range;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fact.auxservice.AuxPoint;
import fact.rta.db.Run;
import fact.rta.rest.LightCurve;
import fact.rta.db.Signal;
import fact.rta.rest.StatusContainer;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;
import stream.Data;
import stream.annotations.Parameter;
import stream.service.Service;
import streams.runtime.Signals;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;


/**
 *
 * TODO: use priority heap for multithreading via xml stream stuff.
 *
 *
 * The Signal and DataRate Processors update stuff in the service.
 * Created by kai on 24.01.16.
 */
public class RTAWebService implements Service {


    RTADataBase.DBInterface dbInterface;

    @Parameter(required = true, description = "Path to the .sqlite file")
    String jdbcConnection;

    final private static Logger log = LoggerFactory.getLogger(RTAWebService.class);


    final private Gson gson;
    private boolean isInit = false;

    private Run currentRun = null;
    private StatusContainer currentStatus = StatusContainer.create();


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


    private ArrayList<Signal> signals = new ArrayList<>();

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

        Spark.get("/lightcurve", (request, response) ->
                {
                    DateTime start = DateTime.parse(request.queryParams("start"));
                    DateTime end = DateTime.parse(request.queryParams("end"));
                    Integer binning = Integer.parseInt(request.queryParams("binning"));

                    return new LightCurve.LightCurveFromDB(dbInterface)
                            .withBinning(binning)
                            .withStartTime(start)
                            .withEndTime(end)
                            .create();

                },
                gson::toJson);

        Spark.get("/datarate",  (request, response) ->
                {
                    DateTime timestamp = DateTime.parse(request.queryParams("timestamp"));
                    return getDataRates(timestamp);
                },
                gson::toJson);


        Spark.get("/event",  (request, response) -> getLatestEvent(), gson::toJson);

        Spark.get("/status", (request, response) -> currentStatus, gson::toJson);

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
                currentStatus = StatusContainer.create();
            }
        }, (long) (0.1 * MINUTE), (long) (0.5*MINUTE));


//        Signals.register(i -> t.cancel());
    }

    public void init(){

        dbInterface = new DBI(this.jdbcConnection).open(RTADataBase.DBInterface.class);

        dbInterface.createRunTable();
        dbInterface.createSignalTable();
        isInit = true;
    }


    synchronized void updateEvent(DateTime eventTimeStamp, Data item, Set<AuxPoint> ftmPointsForNight){
        if (!isInit){
            init();
        }


        Run newRun = new Run(item);
        if (currentRun == null){
            dbInterface.insertRun(newRun);
            currentRun = newRun;
        }
        else if (!currentRun.equals(newRun)){

            log.info("New run found. Fetching ontime of previous run.");

            //I underestimate the actual ontime this way. One could interpolate between the points between two runs.
            double onTimeInSeconds = ftmPointsForNight
                    .stream()
                    .filter(p -> {
                        boolean after = p.getTimeStamp().isAfter(currentRun.startTime);
                        boolean before = p.getTimeStamp().isBefore(currentRun.endTime);
                        return after && before;
                    })
                    .mapToDouble(p -> p.getFloat("OnTime"))
                    .sum();

            dbInterface.updateRunWithOnTime(currentRun, onTimeInSeconds);
            log.info("New run found. OnTime of old run was: {} seconds.", onTimeInSeconds);


            double onTimePerEvent = onTimeInSeconds/signals.size();

            //save signals to database
            persistEvents(signals, onTimePerEvent);
            dbInterface.updateRunHealth(RTADataBase.HEALTH.OK, currentRun.runID, currentRun.night);


            //insert new run to db
            dbInterface.insertRun(newRun);
            dbInterface.updateRunHealth(RTADataBase.HEALTH.IN_PROGRESS, newRun.runID, newRun.night);
            currentRun = newRun;
        }

        signals.add(new Signal(eventTimeStamp, DateTime.now(), item, currentRun));
        eventMap.put(DateTime.now(), new RTAEvent(eventTimeStamp, item));

        Seconds delta = Seconds.secondsBetween(eventMap.firstKey(), eventMap.lastKey());
        if(delta.isGreaterThan(Seconds.seconds(60))){
            eventMap.pollFirstEntry();
        }
    }


    private void persistEvents(ArrayList<Signal> signals, double onTimePerEvent) {
        log.info("Saving stuff to DB");
        if (!isInit){
            init();
        }

        signals.forEach(signal -> {
                    signal.onTimePerEvent = onTimePerEvent;
                    dbInterface.insertSignal(signal);
                });
    }

    private RTAEvent getLatestEvent(){
        if (!eventMap.isEmpty()) {
            return eventMap.lastEntry().getValue();
        }
        return null;
    }

    private ArrayList<RTADataRate> getDataRates(DateTime timeStamp){
        if (rateMap.isEmpty()) {
            return null;
        }

        NavigableMap<DateTime, Double> resultMap;
        if (timeStamp != null) {
            resultMap = rateMap.tailMap(timeStamp, false).descendingMap();
        }
        else {
            resultMap = rateMap.descendingMap();
        }

        ArrayList<RTADataRate> rates = new ArrayList<>();
        resultMap.forEach((k, v) -> rates.add(new RTADataRate(k, v)));
        return rates;
    }

    public void updateDataRate(DateTime timeStamp, Double dataRate){
        rateMap.put(timeStamp, dataRate);
        Seconds delta = Seconds.secondsBetween(rateMap.firstKey(), timeStamp);
        if(delta.isGreaterThan(Seconds.seconds(180))){
            rateMap.pollFirstEntry();
        }
    }


    @Override
    public void reset() throws Exception {
        if(dbInterface != null) {
            dbInterface.close();
        }
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
