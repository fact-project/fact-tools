package fact.rta;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mackaiver on 02/05/17.
 */

@WebSocket
public class DataRateWebSocketHandler {
    final private static Logger log = LoggerFactory.getLogger(DataRateWebSocketHandler.class);

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        WebSocketService.sessions.add(user);
        log.info("Added new session " + user.toString());
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        WebSocketService.sessions.remove(user);
        log.info("Removed session " + user.toString());
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        log.info("Message " + message + " from session " + user.toString());
    }
}
