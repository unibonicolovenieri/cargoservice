package unibo.disi.webgui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * Servizio centrale che mantiene lo stato della stiva e
 * lo propaga a tutti i client WebSocket tramite WebSocketHandlerDemo.
 *
 * Viene aggiornato da CoapObserverService quando arrivano eventi QAktors.
 */
@Service
public class HoldStateService {

    private static final Logger log = Logger.getLogger(HoldStateService.class.getName());

    private final HoldState holdState = new HoldState();
    private final WebSocketHandlerDemo wsHandler;
    private final ObjectMapper mapper = new ObjectMapper();

    public HoldStateService(WebSocketHandlerDemo wsHandler) {
        this.wsHandler = wsHandler;
    }

    // ── Aggiornamenti da eventi CoAP ──────────────────────────────

    /** slot_changed(ID, status) */
    public void onSlotChanged(int slotId, boolean occupied) {
        log.info("[STATE] Slot " + slotId + " -> " + (occupied ? "OCCUPATO" : "LIBERO"));
        holdState.updateSlot(slotId, occupied);
        broadcast();
    }

    /** sonar_changed(status) */
    public void onSonarChanged(String status) {
        log.info("[STATE] Sonar -> " + status);
        holdState.setSonarStatus(status);
        broadcast();
    }

    /** led_changed(status) - "Acceso" o "Spento" */
    public void onLedChanged(String status) {
        boolean on = "Acceso".equalsIgnoreCase(status);
        log.info("[STATE] LED -> " + (on ? "ACCESO" : "SPENTO"));
        holdState.setLedOn(on);
        broadcast();
    }

    /** alarm(X) / problem_solved */
    public void onAlarm(boolean active) {
        log.info("[STATE] ALLARME -> " + (active ? "ATTIVO" : "RISOLTO"));
        holdState.setAlarmActive(active);
        broadcast();
    }

    // ── Broadcast via WebSocket (come sendToAll del prof) ─────────

    private void broadcast() {
        try {
            String json = mapper.writeValueAsString(holdState);
            wsHandler.sendToAll(json);
        } catch (Exception e) {
            log.warning("[STATE] Errore broadcast: " + e.getMessage());
        }
    }

    /** Invia lo stato corrente a un singolo client appena connesso */
    public void sendCurrentStateTo(org.springframework.web.socket.WebSocketSession session) {
        try {
            String json = mapper.writeValueAsString(holdState);
            session.sendMessage(new org.springframework.web.socket.TextMessage(json));
        } catch (Exception e) {
            log.warning("[STATE] Errore invio stato iniziale: " + e.getMessage());
        }
    }
    
    public void onMaxLoad(int maxLoad) {
        log.info("[STATE] MaxLoad -> " + maxLoad);
        holdState.setMaxLoad(maxLoad);
        broadcast();
    }

    public void onWeightAdded(int weight) {
        log.info("[STATE] Peso aggiunto -> " + weight);
        holdState.addWeight(weight);
        broadcast();
    }

    /** current_weight(X) — imposta il peso attuale sulla stiva in modo assoluto */
    public void onCurrentWeight(int weight) {
        log.info("[STATE] Peso corrente (assoluto) -> " + weight + " kg");
        holdState.setCurrentWeight(weight);
        broadcast();
    }

    public HoldState getHoldState() { return holdState; }
}