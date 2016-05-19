package fact.rta;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;
//import fact.rta.persistence.tables.*;
import static fact.rta.persistence.tables.Signal.*;

import fact.rta.persistence.tables.records.SignalRecord;
import org.joda.time.DateTime;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;
import stream.io.SourceURL;
import stream.service.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

/**
 * Created by kai on 24.01.16.
 */
public class RTAWebService implements Service {
//    static Logger log = LoggerFactory.getLogger(RTAWebService.class);

    double datarate = 0;
    TreeRangeMap<DateTime, LightCurve.SignalContainer> lightCurve;

    final Connection conn;
    final DSLContext create;



    public RTAWebService() throws SQLException {
        staticFileLocation("/templates");
        get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("title", "FACT RTA");
            return new ModelAndView(attributes, "index.html");
        }, new HandlebarsTemplateEngine());
        get("/datarate", (request, response) -> datarate);
        get("/lightcurve", (request, response) -> lc());


        String url = "jdbc:sqlite:rta.sqlite";
        conn = DriverManager.getConnection(url, "", "");
        create = DSL.using(conn, SQLDialect.SQLITE);
    }

    /**
     * return the view of the lightcurve
     * @return
     */
    private String lc(){
        if (lightCurve != null) {
            StringJoiner sj = new StringJoiner("-");
            lightCurve.asMapOfRanges().forEach((b, c) -> sj.add(c.signalEvents.toString()));
            return sj.toString();
        }
        return "";
    }




    public void updateDatarate(double rate){
        datarate = rate;
    }

    @Override
    public void reset() throws Exception {

    }


    public void updateLightCurve(TreeRangeMap<DateTime, LightCurve.SignalContainer> lightCurve) {
        this.lightCurve = lightCurve;
        persist(lightCurve);

    }

    private void persist(TreeRangeMap<DateTime, LightCurve.SignalContainer> lightCurve) {

        Float triggerRate = 80.0F;
        Float relativeOnTime = 0.96F;

        InsertValuesStep6<SignalRecord, Timestamp, Integer, Integer, Float, Float, Integer> step =
                create.insertInto(SIGNAL, SIGNAL.TIMESTAMP, SIGNAL.SIGNAL_, SIGNAL.BACKGROUND, SIGNAL.TRIGGER_RATE, SIGNAL.RELATIVE_ON_TIME, SIGNAL.DURATION_IN_SECONDS);

        lightCurve.asDescendingMapOfRanges().forEach((c, b) ->
                step.values(b.getTimestampAsSQLTimeStamp(), b.signalEvents, b.backgroundEvents, triggerRate, relativeOnTime ,b.getDurationInSeconds()));

    }

}
