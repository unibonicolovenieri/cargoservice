package unibo.disi.webgui;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servizio di inizializzazione: al boot invia una richiesta
 *   get_hold_state(webgui_ready)
 * all'attore hold_observer sul contesto webgui (porta CoAP).
 *
 * Rimane in attesa bloccante con retry ogni 3 secondi finché
 * non riceve una risposta valida.
 */
@Component
public class HoldObserverInitService {

    private static final Logger log = Logger.getLogger(HoldObserverInitService.class.getName());

    private static final String REQUEST_PAYLOAD = "get_hold_state(webgui_ready)";
    private static final long RETRY_INTERVAL_MS = 3000L;
    private static final long REQUEST_TIMEOUT_MS = 5000L;

    // Pattern per la risposta iniziale completa da hold_observer
    private static final Pattern HOLD_STATE = Pattern.compile(
        "hold_state\\(slots:([^,]+),maxload:(\\d+),weight:(\\d+),sonar:(\\w+),led:(\\w+),robot:(\\w+)\\)"
    );

    // Inietta HoldStateService direttamente — nessun ciclo
    private final HoldStateService holdStateService;

    @Value("${cargo.coap.host:localhost}")
    private String coapHost;

    @Value("${cargo.coap.port:8000}")
    private int coapPort;

    public HoldObserverInitService(HoldStateService holdStateService) {
        this.holdStateService = holdStateService;
    }

    /**
     * Invia la richiesta iniziale a hold_observer e attende risposta.
     * Blocca il thread corrente finché non arriva una risposta valida.
     * Chiamato PRIMA di avviare gli observer CoAP.
     */
    public void requestInitialState() {
        String uri = "coap://" + coapHost + ":" + coapPort + "/ctx_webgui/hold_observer";
        log.info("[INIT] Invio richiesta iniziale a hold_observer: " + uri);
        log.info("[INIT] Payload: " + REQUEST_PAYLOAD);

        while (true) {
            try {
                CoapClient client = new CoapClient(uri);
                client.setTimeout(REQUEST_TIMEOUT_MS);

                CoapResponse response = client.post(REQUEST_PAYLOAD, MediaTypeRegistry.TEXT_PLAIN);

                if (response != null) {
                    String payload = response.getResponseText();
                    log.info("[INIT] Risposta ricevuta da hold_observer: '" + payload + "'");
                    if (parseHoldState(payload)) {
                        log.info("[INIT] Stato iniziale acquisito. Avvio observers.");
                        return;
                    } else {
                        log.warning("[INIT] Risposta non riconosciuta: '" + payload
                                + "'. Retry tra " + (RETRY_INTERVAL_MS / 1000) + "s...");
                    }
                } else {
                    log.warning("[INIT] Nessuna risposta da hold_observer. Retry tra "
                            + (RETRY_INTERVAL_MS / 1000) + "s...");
                }

            } catch (Exception e) {
                log.warning("[INIT] Errore contattando hold_observer: " + e.getMessage()
                        + ". Retry tra " + (RETRY_INTERVAL_MS / 1000) + "s...");
            }

            try {
                Thread.sleep(RETRY_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.severe("[INIT] Thread interrotto durante attesa retry.");
                return;
            }
        }
    }

    /**
     * Parsa la risposta hold_state(...) e aggiorna lo stato.
     * Restituisce true se il parsing ha avuto successo.
     */
    private boolean parseHoldState(String payload) {
        if (payload == null || payload.isBlank()) return false;

        Matcher m = HOLD_STATE.matcher(payload);
        if (!m.find()) return false;

        String slotsRaw = m.group(1);
        int    maxLoad  = Integer.parseInt(m.group(2));
        int    weight   = Integer.parseInt(m.group(3));
        String sonar    = m.group(4);
        String led      = m.group(5);
        String robot    = m.group(6);

        Map<Integer, Boolean> slots = new HashMap<>();
        for (String entry : slotsRaw.split(";")) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                slots.put(Integer.parseInt(parts[0].trim()),
                          Boolean.parseBoolean(parts[1].trim()));
            }
        }

        holdStateService.onInitialState(slots, maxLoad, weight, sonar, led, robot);
        return true;
    }
}
