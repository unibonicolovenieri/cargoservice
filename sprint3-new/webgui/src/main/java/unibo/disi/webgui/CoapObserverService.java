package unibo.disi.webgui;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        log.warning("[CoAP] L'HO SCRITTO PER TEST 6767676767667 Payload non riconosciuto: " + payload);
    }
}