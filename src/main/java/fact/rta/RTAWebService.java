package fact.rta;

import com.google.common.collect.Range;
import com.google.gson.*;

import fact.auxservice.AuxPoint;
import fact.rta.db.Run;
import fact.rta.rest.*;
import fact.rta.db.Signal;

import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;
import stream.Data;
import stream.annotations.Parameter;
import stream.service.Service;

import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;

import java.time.ZoneOffset;

import java.util.*;


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


    private TreeMap<OffsetDateTime, Event> eventMap = new TreeMap<>();
    private TreeMap<OffsetDateTime, Double> rateMap = new TreeMap<>();


    private ArrayList<Signal> signals = new ArrayList<>();

    public RTAWebService() throws SQLException {

        gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(Range.class, new Serializer.RangeSerializer())
                .registerTypeAdapter(OffsetDateTime.class, new Serializer.DateTimeAdapter())
                .create();


        Spark.staticFileLocation("/rta");

        Spark.get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("title", "FACT RTA - Real Time Analysis");
            return new ModelAndView(attributes, "index.html");
        }, new HandlebarsTemplateEngine("/rta"));


        Spark.get("/lightcurve", (request, response) ->
                {
                    LightCurve.LightCurveFromDB builder = new LightCurve.LightCurveFromDB(dbInterface);

                    if(request.queryParams("start") != null){
                        OffsetDateTime start = OffsetDateTime.parse(request.queryParams("start"));
                        builder.withStartTime(start);
                    }

                    if(request.queryParams("end")!= null){
                        OffsetDateTime end = OffsetDateTime.parse(request.queryParams("end"));
                        builder.withEndTime(end);
                    }
                    if(request.queryParams("binning")!= null){
                        Integer binning = Integer.parseInt(request.queryParams("binning"));
                        builder.withBinning(binning);
                    }

                    return builder.create();
                },
                gson::toJson);


        Spark.get("/datarate",  (request, response) -> getDataRates(request.queryParams("timestamp")), gson::toJson);

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


    }

    public void init(){

        dbInterface = new DBI(this.jdbcConnection).open(RTADataBase.DBInterface.class);

        dbInterface.createRunTableIfNotExists();
        dbInterface.createSignalTableIfNotExists();
        isInit = true;
    }


    synchronized void updateEvent(OffsetDateTime eventTimeStamp, Data item, Set<AuxPoint> ftmPointsForNight){
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
                        OffsetDateTime t = p.getTimeStamp().toGregorianCalendar().toZonedDateTime().toOffsetDateTime();

                        boolean after = t.isAfter(currentRun.startTime);
                        boolean before = t.isAfter(currentRun.endTime);
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

        signals.add(new Signal(eventTimeStamp, OffsetDateTime.now(ZoneOffset.UTC), item, currentRun));
        eventMap.put(OffsetDateTime.now(ZoneOffset.UTC), new Event(eventTimeStamp, item));

        Duration delta = Duration.between(eventMap.firstKey(), eventMap.lastKey());

        if(delta.getSeconds() > 60){
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

    private Event getLatestEvent(){
        if (!eventMap.isEmpty()) {
            return eventMap.lastEntry().getValue();
        }
        return Event.createEmptyEvent();
    }

    private ArrayList<DataRate> getDataRates(String timeStamp){
        if (rateMap.isEmpty()) {
            return new ArrayList<>();
        }

        NavigableMap<OffsetDateTime, Double> resultMap;
        if (timeStamp != null) {
            OffsetDateTime offsetDateTime= OffsetDateTime.parse(timeStamp);
            resultMap = rateMap.tailMap(offsetDateTime, false).descendingMap();
        }
        else {
            resultMap = rateMap.descendingMap();
        }

        ArrayList<DataRate> rates = new ArrayList<>();
        resultMap.forEach((k, v) -> rates.add(new DataRate(k, v)));
        return rates;
    }


    public void updateDataRate(OffsetDateTime timeStamp, Double dataRate){
        rateMap.put(timeStamp, dataRate);
        Duration delta = Duration.between(rateMap.firstKey(), timeStamp);

        if(delta.getSeconds() > 180){
            rateMap.pollFirstEntry();
        }
    }


    @Override
    public void reset() throws Exception {
        if(dbInterface != null) {
            dbInterface.close();
        }
    }

}
