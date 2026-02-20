package unibo.disi.webgui;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST di supporto per test.
 * Equivalente al "TestClient" del prof, ma accessibile via HTTP.
 *
 * POST /simulate  { "payload": "slot_changed(2,true)" }
 * GET  /state     -> stato JSON attuale della stiva
 */
@RestController
@CrossOrigin(origins = "*")
public class SimulateController {

    private final CoapObserverService coapObserverService;
    private final HoldStateService holdStateService;

    public SimulateController(CoapObserverService coapObserverService,
                              HoldStateService holdStateService) {
        this.coapObserverService = coapObserverService;
        this.holdStateService = holdStateService;
    }

    @PostMapping("/simulate")
    public Map<String, String> simulate(@RequestBody Map<String, String> body) {
        String payload = body.getOrDefault("payload", "");
        coapObserverService.parseAndDispatch(payload);
        return Map.of("status", "ok", "payload", payload);
    }

    @GetMapping("/state")
    public HoldState getState() {
        return holdStateService.getHoldState();
    }
}