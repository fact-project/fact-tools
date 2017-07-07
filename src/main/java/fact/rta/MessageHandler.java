package fact.rta;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.gson.*;
import fact.rta.db.Run;
import fact.rta.rest.Event;
import fact.rta.rest.Serializer;
import fact.rta.rest.StatusContainer;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Created by mackaiver on 03/05/17.
 */
public class MessageHandler {


    final private static Logger log = LoggerFactory.getLogger(WebSocketService.class);
    private Gson gson;

    MessageHandler(){
        gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(Range.class, new Serializer.RangeSerializer())
                .registerTypeAdapter(OffsetDateTime.class, new Serializer.DateTimeAdapter())
                .create();
    }

    static Set<Session> sessions = Sets.newConcurrentHashSet();

    void sendDataRate(OffsetDateTime timeStamp, double dataRate){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("timestamp", timeStamp.toString());
        jsonObject.addProperty("date", timeStamp.toString());
        jsonObject.addProperty("rate", dataRate);
        jsonObject.addProperty("topic", "DATA_RATE");

        sendToOpenSessions(jsonObject);
    }

    void sendRunInfo(Run run){
        JsonElement element = gson.toJsonTree(run);
        element.getAsJsonObject().addProperty("topic", "RUN_INFO");
        sendToOpenSessions(element);
    }

    void sendStatus(StatusContainer status){
        JsonElement element = gson.toJsonTree(status);
        element.getAsJsonObject().addProperty("topic", "MACHINE_STATUS");
        sendToOpenSessions(element);
    }
    void sendEvent(Event event){
        JsonElement element = gson.toJsonTree(event);
        element.getAsJsonObject().addProperty("topic", "EVENT");
        sendToOpenSessions(element);
    }

    public void sendDataStatus(String s) {
        JsonElement element = new JsonObject();
        element.getAsJsonObject().addProperty("topic", "DATA_STATUS");
        element.getAsJsonObject().addProperty("status", s);
        sendToOpenSessions(element);
    }

    private void sendToOpenSessions(JsonElement element) {
        sessions.stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(gson.toJson(element));
            } catch (IOException e) {
                log.error("Error sending event to session " + session);
                e.printStackTrace();
            } catch (IllegalStateException e){
                log.error("Error sending event to session. Illegal State in session:" + session);
                e.printStackTrace();
            }
        });
    }

}
