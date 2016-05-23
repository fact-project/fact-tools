package fact.rta;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.TreeRangeMap;
import static fact.rta.persistence.tables.Signal.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fact.rta.persistence.tables.records.SignalRecord;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;
import stream.service.Service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;


/**
 * Created by kai on 24.01.16.
 */
public class RTAWebService implements Service {
    private static Logger log = LoggerFactory.getLogger(RTAWebService.class);

    private Runtime runtime = Runtime.getRuntime();
    private Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeAdapter()).create();


    private StatusContainer currentStatus;


    private class StatusContainer{
        final long usedMemory;
        final long memoryLimit;
        final int availableProcessors;
        final long totalSpace;
        final long freeSpace;
        final DateTime timeStamp;

        public StatusContainer(){
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            //convert to mega bytes
            usedMemory /= 1024L * 1024L;

            long memoryLimit = runtime.maxMemory();
            memoryLimit /= 1024L * 1024L;

            int availableProcessors = runtime.availableProcessors();

            long totalSpace = 0;
            long freeSpace = 0;
            File[] roots = File.listRoots();
            for (File root : roots) {
                totalSpace += root.getTotalSpace();
                freeSpace += root.getFreeSpace();
            }
            //to GB
            totalSpace /= 1024L * 1024L * 1024L;
            freeSpace  /= 1024L * 1024L * 1024L;

            this.availableProcessors = availableProcessors;
            this.freeSpace = freeSpace;
            this.memoryLimit = memoryLimit;
            this.totalSpace = totalSpace;
            this.usedMemory = usedMemory;
            this.timeStamp = DateTime.now();
        }
    }

    private class DataRate{
        final Double rate;
        final DateTime timeStamp;

        DataRate(DateTime timeStamp, Double rate) {
            this.rate = rate;
            this.timeStamp = timeStamp;
        }
    }

    private class RTAEvent{
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

    private EvictingQueue<RTAEvent> eventQueue = EvictingQueue.create(250);
    private TreeMap<DateTime, Double> rateMap = new TreeMap<>();
    private EvictingQueue<StatusContainer> systemStatusQueue = EvictingQueue.create(100);

    private TreeRangeMap<DateTime, RTAProcessor.SignalContainer> lightCurve;


    private final Connection conn;
    private final DSLContext create;



    public RTAWebService() throws SQLException {
        Spark.staticFileLocation("/rta");
        Spark.get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("title", "FACT RTA - Real Time Analysis");
            return new ModelAndView(attributes, "rta/index.html");
        }, new HandlebarsTemplateEngine());

//        Spark.get("/lightcurve", (request, response) -> lc());

        Spark.get("/datarate", (request, response) -> getDataRates(request.queryParams("timeStamp")), gson::toJson);

//        Spark.get("/event", (request, response) -> currentEvent, gson::toJson);
//
//        Spark.get("/status", (request, response) -> getSystemStatus(), gson::toJson);


        String url = "jdbc:sqlite:rta.sqlite";
        conn = DriverManager.getConnection(url, "", "");
        create = DSL.using(conn, SQLDialect.SQLITE);

        StatusContainer c = new StatusContainer();
        systemStatusQueue.add(c);
        currentStatus = c;
    }




    public void updateEvent(double[] photoncharges,double  estimatedEnergy, double size, double thetaSquare, String sourceName, DateTime eventTimeStamp){
        RTAEvent event = new RTAEvent(photoncharges, estimatedEnergy, size, thetaSquare, sourceName, eventTimeStamp);
        eventQueue.add(event);
    }


    public ArrayList<DataRate> getDataRates(String timeStamp){
        ArrayList<DataRate> l = new ArrayList<>();
        if (timeStamp != null) {
            try {
                rateMap.descendingMap().headMap(DateTime.parse(timeStamp)).forEach((k, v) -> l.add(new DataRate(k,v)));
                return l;
            } catch (IllegalArgumentException e) {
                return l;
            }
        }
        rateMap.descendingMap().forEach((k, v) -> l.add(new DataRate(k, v)));
        return l;
    }

    public void updateDataRate(DateTime timeStamp, Double dataRate){
        rateMap.put(timeStamp, dataRate);
        Seconds delta = Seconds.secondsBetween(rateMap.lastKey(), timeStamp);
        if(delta.isGreaterThan(Seconds.seconds(60))){
            rateMap.pollFirstEntry();
        }
    }

    /**
     * return the view of the lightcurve
     * @return nothing yet
     */
    private String lc(){

        if (lightCurve != null){
            StringJoiner sj = new StringJoiner("-");
            lightCurve.asMapOfRanges().forEach((b, c) -> sj.add(c.signalEvents.toString()));
            return sj.toString();
        }
        return "";
    }


    private StatusContainer getSystemStatus(){
        DateTime now = DateTime.now();
        Seconds seconds = Seconds.secondsBetween(currentStatus.timeStamp, now);
        System.out.println(seconds.getSeconds());
        if (seconds.isGreaterThan(Seconds.seconds(4))){
            StatusContainer c = new StatusContainer();
            currentStatus = c;
            systemStatusQueue.add(c);
            System.out.println("updated status");
        }
        return currentStatus;
    }




    @Override
    public void reset() throws Exception {

    }


    public void updateLightCurve(TreeRangeMap<DateTime, RTAProcessor.SignalContainer> lightCurve) {
        this.lightCurve = lightCurve;
        persist(lightCurve);

    }

    private void persist(TreeRangeMap<DateTime, RTAProcessor.SignalContainer> lightCurve) {

        Float triggerRate = 80.0F;
        Float relativeOnTime = 0.96F;

        InsertValuesStep6<SignalRecord, Timestamp, Integer, Integer, Float, Float, Integer> step =
                create.insertInto(SIGNAL, SIGNAL.TIMESTAMP, SIGNAL.SIGNAL_, SIGNAL.BACKGROUND, SIGNAL.TRIGGER_RATE, SIGNAL.RELATIVE_ON_TIME, SIGNAL.DURATION_IN_SECONDS);

        lightCurve.asDescendingMapOfRanges().forEach((c, b) ->
                step.values(b.getTimestampAsSQLTimeStamp(), b.signalEvents, b.backgroundEvents, triggerRate, relativeOnTime ,b.getDurationInSeconds()));

    }

    public static class DateTimeAdapter extends TypeAdapter<DateTime> {

        @Override
        public void write(JsonWriter jsonWriter, DateTime dateTime) throws IOException {
            if (dateTime == null){
                jsonWriter.nullValue();
            }
            else{
                jsonWriter.value(dateTime.toString("y-M-d'T'H:mm:s.SZ"));
            }
        }

        @Override
        public DateTime read(JsonReader jsonReader) throws IOException {
            return DateTime.parse(jsonReader.toString());
        }
    }


}
