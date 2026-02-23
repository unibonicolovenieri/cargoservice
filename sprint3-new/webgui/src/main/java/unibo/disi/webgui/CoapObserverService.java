package unibo.disi.webgui;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Osserva via CoAP le risorse esposte da QAktors.
 *
 * Risorse osservate:
 *   coap://<host>:<port>/ctx_cargoservice/hold_observer -> hold_state(SNAPSHOT)
 *   coap://<host>:<port>/ctx_cargo/cargoservice         -> slot_changed, led_changed, sonar_changed
 *   coap://<host>:<port>/ctx_cargo/cargorobot           -> alarm, problem_solved
 *
 * Logica di bootstrap:
 *   - La prima hold_state ricevuta da hold_observer viene applicata come snapshot
 *     iniziale dell'intera stiva (tutti i campi in un colpo).
 *   - Ogni ulteriore hold_state viene SCARTATA: da quel momento in poi vengono
 *     processate esclusivamente le update incrementali (slot_changed, led_changed, …).
 */
@Component
public class CoapObserverService {

    private static final Logger log = Logger.getLogger(CoapObserverService.class.getName());

    // ── Pattern per update incrementali ───────────────────────────
    private static final Pattern SLOT_CHANGED   = Pattern.compile("slot_changed\\((\\d+),(\\w+)\\)");
    private static final Pattern SONAR_CHANGED  = Pattern.compile("sonar_changed\\((\\w+)\\)");
    private static final Pattern LED_CHANGED    = Pattern.compile("led_changed\\((\\w+)\\)");
    private static final Pattern ALARM          = Pattern.compile("alarm\\((\\w+)\\)");
    private static final Pattern MAXLOAD        = Pattern.compile("maxload\\(\\s*(\\d+)\\s*\\)");
    private static final Pattern WEIGHT         = Pattern.compile("\\bweight\\(\\s*(\\d+)\\s*\\)");
    private static final Pattern CURRENT_WEIGHT = Pattern.compile("current_weight\\(\\s*(\\d+)\\s*\\)");

    // ── Pattern per lo snapshot iniziale da hold_observer ─────────
    // Formato atteso: hold_state(slot_1=true,slot_2=false,...,sonar=DFREE,led=Acceso,alarm=false,maxload=500,weight=120)
    private static final Pattern HOLD_STATE = Pattern.compile("hold_state\\((.+)\\)");

    /**
     * Flag thread-safe: diventa true dopo che la prima hold_state è stata
     * applicata. Da quel momento ogni successiva hold_state viene ignorata.
     */
    private final AtomicBoolean holdStateReceived = new AtomicBoolean(false);

    private final HoldStateService holdStateService;

    @Value("${cargo.coap.host:localhost}")
    private String coapHost;

    @Value("${cargo.coap.port:8000}")
    private int coapPort;

    public CoapObserverService(HoldStateService holdStateService) {
        this.holdStateService = holdStateService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startObserving() {
        log.info("[CoAP] Avvio observe su " + coapHost + ":" + coapPort);
        // Risorsa hold_observer: invia sporadicamente lo snapshot completo
        observeResource("ctx_cargoservice/hold_observer");
        // Risorse incrementali già esistenti
        observeResource("ctx_cargo/cargoservice");
        observeResource("ctx_cargo/cargorobot");
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
                new Thread(() -> {
                    try { Thread.sleep(5000); observeResource(resource); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }).start();
            }
        });
    }

    /**
     * Parsing payload QAktors e dispatch all'HoldStateService.
     *
     * Priorità:
     * 1. Se il payload è un hold_state(...):
     *    - Prima volta → applicato come snapshot iniziale, flag impostato.
     *    - Volte successive → SCARTATO (log di info, nessun aggiornamento).
     * 2. Altrimenti → routing normale sulle update incrementali.
     *
     * Esposto come public per permettere simulazione via REST (senza QAktors attivo).
     */
    public void parseAndDispatch(String payload) {
        if (payload == null || payload.isBlank()) return;

        // ── Gestione hold_state (snapshot da hold_observer) ───────
        Matcher hsm = HOLD_STATE.matcher(payload);
        if (hsm.find()) {
            if (holdStateReceived.compareAndSet(false, true)) {
                // Prima hold_state: la applico come snapshot iniziale
                String snapshotText = hsm.group(1);
                log.info("[CoAP] Prima hold_state ricevuta — applico snapshot: " + snapshotText);
                holdStateService.onHoldStateSnapshot(snapshotText);
            } else {
                // hold_state già ricevuta in precedenza: la scartiamo
                log.info("[CoAP] hold_state ignorata (snapshot già acquisito)");
            }
            return;
        }

        // ── Update incrementali (processate solo dopo il bootstrap) ─
        Matcher m;

        m = SLOT_CHANGED.matcher(payload);
        if (m.find()) {
            int id  = Integer.parseInt(m.group(1));
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

        log.warning("[CoAP] Payload non riconosciuto: " + payload);
    }

    /** Esposto per i test: permette di resettare il flag come se non fosse mai arrivata una hold_state. */
    public void resetHoldStateFlag() {
        holdStateReceived.set(false);
        log.info("[CoAP] Flag holdStateReceived resettato");
    }

    /** Restituisce true se è già stata ricevuta almeno una hold_state. */
    public boolean isHoldStateReceived() {
        return holdStateReceived.get();
    }
}