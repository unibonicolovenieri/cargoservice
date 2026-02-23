package unibo.disi.webgui;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * Osserva via CoAP le risorse esposte da QAktors (ctx_cargo porta 8000).
 *
 * Quando un attore QAktors esegue un "emit", aggiorna la sua risorsa CoAP
 * e Californium notifica automaticamente questo observer.
 *
 * Risorse osservate:
 *   coap://<host>:8000/cargoservice  -> slot_changed, led_changed, sonar_changed
 *   coap://<host>:8000/cargorobot   -> alarm, problem_solved
 */
@Component
public class CoapObserverService {

    private static final Logger log = Logger.getLogger(CoapObserverService.class.getName());

    // Pattern per parsing messaggi QAktors
    private static final Pattern SLOT_CHANGED  = Pattern.compile("slot_changed\\((\\d+),(\\w+)\\)");
    private static final Pattern SONAR_CHANGED = Pattern.compile("sonar_changed\\((\\w+)\\)");
    private static final Pattern LED_CHANGED   = Pattern.compile("led_changed\\((\\w+)\\)");
    private static final Pattern ALARM         = Pattern.compile("alarm\\((\\w+)\\)");
    private static final Pattern MAXLOAD = Pattern.compile("maxload\\(\\s*(\\d+)\\s*\\)");
    // "weight" usa \b (word boundary) per non matchare su "current_weight"
    private static final Pattern WEIGHT        = Pattern.compile("\\bweight\\(\\s*(\\d+)\\s*\\)");
    private static final Pattern CURRENT_WEIGHT = Pattern.compile("current_weight\\(\\s*(\\d+)\\s*\\)");

    // Pattern per la risposta iniziale completa da hold_observer
    private static final Pattern HOLD_STATE = Pattern.compile(
        "hold_state\\(slots:([^,]+),maxload:(\\d+),weight:(\\d+),sonar:(\\w+),led:(\\w+),robot:(\\w+)\\)"
    );

    private final HoldStateService holdStateService;
    private final HoldObserverInitService holdObserverInitService;

    @Value("${cargo.coap.host:localhost}")
    private String coapHost;

    @Value("${cargo.coap.port:8000}")
    private int coapPort;

    public CoapObserverService(HoldStateService holdStateService,
                               @org.springframework.context.annotation.Lazy HoldObserverInitService holdObserverInitService) {
        this.holdStateService = holdStateService;
        this.holdObserverInitService = holdObserverInitService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startObserving() {
        // 1. Prima operazione: richiesta iniziale a hold_observer (bloccante fino a risposta)
        holdObserverInitService.requestInitialState();

        // 2. Solo dopo: avvio degli observer CoAP
        log.info("[CoAP] Avvio observe su " + coapHost + ":" + coapPort);
        observeResource("ctx_cargo/sonar_test");
        observeResource("ctx_cargo/cargorobot");
        observeResource("ctx_cargo/sonar_test");
    }

    private void observeResource(String resource) {
        String uri = "coap://" + coapHost + ":" + coapPort + "/" + resource;
        log.info("[CoAP] Osservo: " + uri);

        CoapClient client = new CoapClient(uri);
        client.setTimeout(5000L);

        client.observe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    String payload = response.getResponseText();
                    log.info("[CoAP] RAW payload da " + resource + ": '" + payload + "'");
                    parseAndDispatch(payload);
                }
            }

            @Override
            public void onError() {
                log.warning("[CoAP] Errore connessione a " + uri);
                // Retry automatico dopo 5 secondi
                new Thread(() -> {
                    try { Thread.sleep(5000); observeResource(resource); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }).start();
            }
        });
    }

    /**
     * Parsing payload QAktors e dispatch all'HoldStateService.
     * Formato: "slot_changed(2,true)", "led_changed(Acceso)", ecc.
     *
     * Esposto come public per permettere simulazione via REST (senza QAktors attivo).
     */
    public void parseAndDispatch(String payload) {
        if (payload == null || payload.isBlank()) return;

        Matcher m;

        // Risposta iniziale completa da hold_observer — gestita per prima
        m = HOLD_STATE.matcher(payload);
        if (m.find()) {
            String slotsRaw  = m.group(1); // es: "1=false;2=false;3=true;4=false"
            int    maxLoad   = Integer.parseInt(m.group(2));
            int    weight    = Integer.parseInt(m.group(3));
            String sonar     = m.group(4);
            String led       = m.group(5);
            String robot     = m.group(6);

            // Parsing slot: "1=false;2=true;..."
            Map<Integer, Boolean> slots = new HashMap<>();
            for (String entry : slotsRaw.split(";")) {
                String[] parts = entry.split("=");
                if (parts.length == 2) {
                    slots.put(Integer.parseInt(parts[0].trim()),
                              Boolean.parseBoolean(parts[1].trim()));
                }
            }

            holdStateService.onInitialState(slots, maxLoad, weight, sonar, led, robot);
            return;
        }

        m = SLOT_CHANGED.matcher(payload);
        if (m.find()) {
            int id = Integer.parseInt(m.group(1));
            boolean occ = Boolean.parseBoolean(m.group(2)) || "true".equalsIgnoreCase(m.group(2));
            holdStateService.onSlotChanged(id, occ);
            return;
        }

        m = LED_CHANGED.matcher(payload);
        if (m.find()) { holdStateService.onLedChanged(m.group(1)); return; }

        m = SONAR_CHANGED.matcher(payload);
        if (m.find()) { holdStateService.onSonarChanged(m.group(1)); return; }

        m = ALARM.matcher(payload);
        if (m.find()) { holdStateService.onAlarm(true); return; }

        if (payload.contains("problem_solved")) { holdStateService.onAlarm(false); return; }
        
        m = MAXLOAD.matcher(payload);
        if (m.find()) { holdStateService.onMaxLoad(Integer.parseInt(m.group(1))); return; }

        m = CURRENT_WEIGHT.matcher(payload);
        if (m.find()) { holdStateService.onCurrentWeight(Integer.parseInt(m.group(1))); return; }

        m = WEIGHT.matcher(payload);
        if (m.find()) { holdStateService.onWeightAdded(Integer.parseInt(m.group(1))); return; }

        log.warning("[CoAP] L'HO SCRITTO PER TEST 6767676767667 Payload non riconosciuto: " + payload);
    }
}