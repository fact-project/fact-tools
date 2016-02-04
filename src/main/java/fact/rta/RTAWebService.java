package fact.rta;

import com.google.common.collect.TreeRangeMap;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;
import stream.io.SourceURL;
import stream.service.Service;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

/**
 * Created by kai on 24.01.16.
 */
public class RTAWebService implements Service {
    static Logger log = LoggerFactory.getLogger(RTAWebService.class);

    double datarate = 0;
    TreeRangeMap<DateTime, Double> lightCurve;
    SourceURL url;

    public RTAWebService() {
        staticFileLocation("/templates");
        get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("title", "FACT RTA");
            return new ModelAndView(attributes, "index.html");
        }, new HandlebarsTemplateEngine());
        get("/datarate", (request, response) -> String.format("%f events per second.", datarate));
        get("/lightcurve", (request, response) -> String.format("%f events per second.", datarate));

        try {
            url = new SourceURL("file:/rta.sqlite");
        } catch (Exception e) {
            log.error("could not open Database. WTF Mate?");
        }

    }






    public void updateDatarate(double rate){
        datarate = rate;
    }

    @Override
    public void reset() throws Exception {

    }


    public void updateLightCurve(TreeRangeMap<DateTime, Double> lightCurve) {
        this.lightCurve = lightCurve;

    }
}
