package unibo.disi.webgui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that exercises all major components:
 * - HoldState (data model)
 * - HoldStateService (state management & broadcast)
 * - CoapObserverService (parsing & dispatch)
 * - WebSocketHandlerDemo (WebSocket connections)
 * - SimulateController (REST API for testing)
 */
@SpringBootTest
class WebguiApplicationTests {

	@Autowired
	private HoldStateService holdStateService;

	@Autowired
	private CoapObserverService coapObserverService;

	@Autowired
	private WebSocketHandlerDemo webSocketHandler;

	@Autowired
	private SimulateController simulateController;

	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		// Reset state before each test
		HoldState state = holdStateService.getHoldState();
		for (int i = 1; i <= 4; i++) {
			state.updateSlot(i, false);
		}
		state.setSonarStatus("DFREE");
		state.setLedOn(false);
		state.setAlarmActive(false);
		state.setMaxLoad(-1);
		state.setCurrentWeight(0);
	}

	@Test
	void testHoldStateInitialization() {
		HoldState state = new HoldState();
		assertNotNull(state.getSlots());
		assertEquals(4, state.getSlots().size());
		assertEquals("DFREE", state.getSonarStatus());
		assertFalse(state.isLedOn());
		assertFalse(state.isAlarmActive());
		assertEquals(-1, state.getMaxLoad());
		assertEquals(0, state.getCurrentWeight());
	}

	@Test
	void testHoldStateSlotUpdate() {
		HoldState state = new HoldState();
		state.updateSlot(2, true);
		assertTrue(state.getSlots().get(2));
		state.updateSlot(2, false);
		assertFalse(state.getSlots().get(2));
	}

	@Test
	void testHoldStateWeightTracking() {
		HoldState state = new HoldState();
		state.setMaxLoad(100);
		state.addWeight(25);
		assertEquals(25, state.getCurrentWeight());
		state.addWeight(15);
		assertEquals(40, state.getCurrentWeight());
		state.setCurrentWeight(60);
		assertEquals(60, state.getCurrentWeight());
	}


	@Test
	void testCoapParseSlotChanged() {
		coapObserverService.parseAndDispatch("slot_changed(2,true)");
		assertTrue(holdStateService.getHoldState().getSlots().get(2));

		coapObserverService.parseAndDispatch("slot_changed(3,false)");
		assertFalse(holdStateService.getHoldState().getSlots().get(3));
	}

	@Test
	void testCoapParseSonarChanged() {
		coapObserverService.parseAndDispatch("sonar_changed(Container_Individuato)");
		assertEquals("Container_Individuato", holdStateService.getHoldState().getSonarStatus());

		coapObserverService.parseAndDispatch("sonar_changed(DFREE)");
		assertEquals("DFREE", holdStateService.getHoldState().getSonarStatus());
	}

	@Test
	void testCoapParseLedChanged() {
		coapObserverService.parseAndDispatch("led_changed(Acceso)");
		assertTrue(holdStateService.getHoldState().isLedOn());

		coapObserverService.parseAndDispatch("led_changed(Spento)");
		assertFalse(holdStateService.getHoldState().isLedOn());
	}

	@Test
	void testCoapParseAlarm() {
		coapObserverService.parseAndDispatch("alarm(true)");
		assertTrue(holdStateService.getHoldState().isAlarmActive());

		coapObserverService.parseAndDispatch("problem_solved");
		assertFalse(holdStateService.getHoldState().isAlarmActive());
	}

	@Test
	void testCoapParseWeight() {
		coapObserverService.parseAndDispatch("maxload(150)");
		assertEquals(150, holdStateService.getHoldState().getMaxLoad());

		coapObserverService.parseAndDispatch("weight(50)");
		assertEquals(50, holdStateService.getHoldState().getCurrentWeight());

		coapObserverService.parseAndDispatch("current_weight(75)");
		assertEquals(75, holdStateService.getHoldState().getCurrentWeight());
	}

	@Test
	void testCoapParseInvalidPayload() {
		// Should not crash, just log warning
		coapObserverService.parseAndDispatch("invalid_payload_xyz");
		coapObserverService.parseAndDispatch("");
		coapObserverService.parseAndDispatch(null);
	}


	@Test
	void testHoldStateServiceUpdateSlot() {
		holdStateService.onSlotChanged(1, true);
		assertTrue(holdStateService.getHoldState().getSlots().get(1));
		// WebSocket clients are 0 since we haven't established live connections in this test
		assertEquals(0, webSocketHandler.getConnectedClients());
	}

	@Test
	void testHoldStateServiceUpdateSonar() {
		holdStateService.onSonarChanged("BLOCKED");
		assertEquals("BLOCKED", holdStateService.getHoldState().getSonarStatus());
	}

	@Test
	void testHoldStateServiceUpdateLed() {
		holdStateService.onLedChanged("Acceso");
		assertTrue(holdStateService.getHoldState().isLedOn());

		holdStateService.onLedChanged("Spento");
		assertFalse(holdStateService.getHoldState().isLedOn());
	}

	@Test
	void testHoldStateServiceUpdateAlarm() {
		holdStateService.onAlarm(true);
		assertTrue(holdStateService.getHoldState().isAlarmActive());

		holdStateService.onAlarm(false);
		assertFalse(holdStateService.getHoldState().isAlarmActive());
	}


	@Test
	void testSimulateControllerLogic() {
		// Test that simulate controller routes to CoapObserverService properly
		String payload = "slot_changed(2,true)";
		coapObserverService.parseAndDispatch(payload);
		assertTrue(holdStateService.getHoldState().getSlots().get(2));
	}

	@Test
	void testGetStateReturnsCurrentState() {
		// Set some state first
		holdStateService.onSlotChanged(1, true);
		holdStateService.onSonarChanged("DFREE");
		holdStateService.onLedChanged("Acceso");

		HoldState state = holdStateService.getHoldState();
		assertTrue(state.getSlots().get(1));
		assertEquals("DFREE", state.getSonarStatus());
		assertTrue(state.isLedOn());
	}

	@Test
	void testSimulateMultipleEvents() {
		// Test event 1
		coapObserverService.parseAndDispatch("slot_changed(1,true)");
		// Test event 2
		coapObserverService.parseAndDispatch("sonar_changed(Container_Individuato)");

		// Verify cumulative state
		HoldState state = holdStateService.getHoldState();
		assertTrue(state.getSlots().get(1));
		assertEquals("Container_Individuato", state.getSonarStatus());
	}


	@Test
	void testWebSocketHandlerClientCount() {
		assertEquals(0, webSocketHandler.getConnectedClients());
	}

	@Test
	void testWebSocketSendToAll() {
		// This is a basic test; actual WS sessions are hard to mock inline
		// But we verify the handler exists and can be called
		assertNotNull(webSocketHandler);
		webSocketHandler.sendToAll("test message");
	}

	// ════════════════════════════════════════════════════════════
	// 5. TEST SimulateController (REST API endpoints)
	// ════════════════════════════════════════════════════════════

	@Test
	void testSimulateControllerSimulateEndpoint() {
		Map<String, String> request = Map.of("payload", "slot_changed(2,true)");
		Map<String, String> response = simulateController.simulate(request);

		assertEquals("ok", response.get("status"));
		assertEquals("slot_changed(2,true)", response.get("payload"));
		assertTrue(holdStateService.getHoldState().getSlots().get(2));
	}

	@Test
	void testSimulateControllerGetStateEndpoint() {
		holdStateService.onSlotChanged(1, true);
		holdStateService.onSonarChanged("BLOCKED");
		holdStateService.onLedChanged("Acceso");

		HoldState state = simulateController.getState();

		assertNotNull(state);
		assertTrue(state.getSlots().get(1));
		assertEquals("BLOCKED", state.getSonarStatus());
		assertTrue(state.isLedOn());
	}

	@Test
	void testSimulateControllerMultiplePayloads() {
		// Simulate multiple events via controller
		simulateController.simulate(Map.of("payload", "maxload(150)"));
		simulateController.simulate(Map.of("payload", "slot_changed(1,true)"));
		simulateController.simulate(Map.of("payload", "weight(50)"));

		HoldState state = simulateController.getState();
		assertEquals(150, state.getMaxLoad());
		assertTrue(state.getSlots().get(1));
		assertEquals(50, state.getCurrentWeight());
	}
	@Test
	void testEndToEndSlotFlow() {
		// Simulate a complete flow: CoapObserverService → HoldStateService
		coapObserverService.parseAndDispatch("slot_changed(3,true)");

		// Verify state is updated
		HoldState state = holdStateService.getHoldState();
		assertTrue(state.getSlots().get(3));
		assertNotNull(state.getLastUpdate());
	}

	@Test
	void testEndToEndComplexScenario() {
		// Simulate multiple events in sequence
		coapObserverService.parseAndDispatch("maxload(200)");
		coapObserverService.parseAndDispatch("slot_changed(1,true)");
		coapObserverService.parseAndDispatch("slot_changed(2,true)");
		coapObserverService.parseAndDispatch("sonar_changed(Container_Individuato)");
		coapObserverService.parseAndDispatch("led_changed(Acceso)");
		coapObserverService.parseAndDispatch("weight(75)");

		HoldState state = holdStateService.getHoldState();
		assertEquals(200, state.getMaxLoad());
		assertTrue(state.getSlots().get(1));
		assertTrue(state.getSlots().get(2));
		assertFalse(state.getSlots().get(3));
		assertEquals("Container_Individuato", state.getSonarStatus());
		assertTrue(state.isLedOn());
		assertEquals(75, state.getCurrentWeight());
	}
}

