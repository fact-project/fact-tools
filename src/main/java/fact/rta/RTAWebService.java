package fact.rta;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.TreeRangeMap;
import static fact.rta.persistence.tables.Signal.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fact.rta.persistence.tables.records.SignalRecord;
import org.joda.time.DateTime;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;
import stream.service.Service;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * Created by kai on 24.01.16.
 */
public class RTAWebService implements Service {
    static Logger log = LoggerFactory.getLogger(RTAWebService.class);

    double datarate = 0;
    private Map event;

    Runtime runtime = Runtime.getRuntime();
    Gson gson = new GsonBuilder().create();

    Cache<String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();



    TreeRangeMap<DateTime, RTAProcessor.SignalContainer> lightCurve;

    final Connection conn;
    final DSLContext create;



    public RTAWebService() throws SQLException {
        Spark.staticFileLocation("/rta");
        Spark.get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("title", "FACT RTA - Real Time Analysis");
            return new ModelAndView(attributes, "rta/index.html");
        }, new HandlebarsTemplateEngine());
        Spark.get("/datarate", (request, response) -> datarate);
        Spark.get("/lightcurve", (request, response) -> lc());
        Spark.get("/event", (request, response) -> getEvent());
        Spark.get("/status", (request, response) -> getStatus());


        String url = "jdbc:sqlite:rta.sqlite";
        conn = DriverManager.getConnection(url, "", "");
        create = DSL.using(conn, SQLDialect.SQLITE);
    }

    /**
     * return the view of the lightcurve
     * @return
     */
    private String lc(){

        if (lightCurve != null){
            StringJoiner sj = new StringJoiner("-");
            lightCurve.asMapOfRanges().forEach((b, c) -> sj.add(c.signalEvents.toString()));
            return sj.toString();
        }
        return "";
    }


    /**
     * Collect system information and return information as json.
     *
     * @return a json string
     */
    private String getStatus(){
        String status = "{}";
        try {
            status = cache.get("status", () -> {
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

                Map m = new HashMap<String, Number>();
                m.put("usedMemory", usedMemory);
                m.put("memoryLimit", memoryLimit);
                m.put("availableProcessors", availableProcessors);
                m.put("totalSpace", totalSpace);
                m.put("freeSpace", freeSpace);

                return gson.toJson(m);
            });
        } catch (ExecutionException e) {
            log.error("Cannot load system status to cache.");
        }
        return status;
    }

    /**
     * The last event which was "interesting". A collection of photoncharges.
     * @return json array conatineing the photoncharges.
     */
    private String getEvent(){
        return gson.toJson(event);
    }


    public void updateEvent(double[] photonCharges, double estimatedEnergy, double size, double thetaSquare, String sourceName, String timestamp) {
        Map m = new HashMap<String, Serializable>();
        m.put("image", photonCharges);
        m.put("energy", estimatedEnergy);
        m.put("Size", size);
        m.put("thetasquare", thetaSquare);
        m.put("timestamp", timestamp);
        m.put("sourceName", sourceName);
        event = m;
    }

    public void updateDatarate(double rate){
        datarate = rate;
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


}
