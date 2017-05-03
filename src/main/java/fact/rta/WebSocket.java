package fact.rta;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mackaiver on 03/05/17.
 */
@org.eclipse.jetty.websocket.api.annotations.WebSocket
public class WebSocket {
    final private Logger log = LoggerFactory.getLogger(WebSocket.class);
    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        MessageHandler.sessions.add(user);
        log.info("Added new session " + user.toString());
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        MessageHandler.sessions.remove(user);
        log.info("Removed session " + user.toString());
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        log.info("Ignoring message " + message + " from session " + user.toString());
    }

}
