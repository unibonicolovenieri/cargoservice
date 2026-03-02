package unibo.disi.webgui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * "Il gestore WebSocketHandlerDemo" - come da doc professore.
 *
 * Estende AbstractWebSocketHandler (come mostrato nel doc).
 * Mantiene la lista delle sessioni per la propagazione a tutti i client.
 *
 * Aggiornamenti dello stato della stiva (da CoapObserverService)
 * vengono inviati a tutti i client via sendToAll().
 */
@Component
public class WebSocketHandlerDemo extends AbstractWebSocketHandler {

    private static final Logger log = Logger.getLogger(WebSocketHandlerDemo.class.getName());
    private final CoapObserverService coapObserverService;

    public WebSocketHandlerDemo(@Lazy CoapObserverService coapObserverService) {
        this.coapObserverService = coapObserverService;
    }

    // "Propagazione a tutti i client" - lista sessioni come da doc prof
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();

    // ── Gestione connessioni (come da doc professore) ──────────────

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        super.afterConnectionEstablished(session);
        log.info("[WS] Client connesso: " + session.getId() + " | Totale: " + sessions.size());

        // Invia stato attuale al nuovo client appena si connette
        // (lo stato viene recuperato via HoldStateService se disponibile)
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        super.afterConnectionClosed(session, status);
        log.info("[WS] Client disconnesso: " + session.getId() + " | Rimasti: " + sessions.size());
    }

    // ── Gestione messaggi in arrivo dal browser ───────────────────

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String cmd = message.getPayload();
        log.info("[WS] Messaggio ricevuto: " + cmd);
        // Passa al CoapObserverService per il parsing invece di fare solo echo
        coapObserverService.parseAndDispatch(cmd);
    }

    // ── Propagazione a tutti i client (come da doc professore) ────

    /**
     * Invia un messaggio testuale a tutti i client connessi.
     * Chiamato da CoapObserverService quando arriva un evento QAktors.
     */
    public void sendToAll(String message) {
        TextMessage textMessage = new TextMessage(message);
        sendToAll(textMessage);
    }

    public void sendToAll(TextMessage message) {
        Iterator<WebSocketSession> iter = sessions.iterator();
        while (iter.hasNext()) {
            WebSocketSession s = iter.next();
            try {
                if (s.isOpen()) {
                    s.sendMessage(message);
                }
            } catch (IOException e) {
                log.warning("[WS] Errore invio a " + s.getId() + ": " + e.getMessage());
            }
        }
    }

    public int getConnectedClients() {
        return sessions.size();
    }
}