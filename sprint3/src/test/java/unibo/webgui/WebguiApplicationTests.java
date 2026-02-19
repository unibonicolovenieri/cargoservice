package unibo.webgui;

import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.Interaction;
import unibo.webgui.ws.WSHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WebguiApplicationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private Interaction mockConnection;

    @Autowired
    private WSHandler wsHandler;

    private HttpClient httpClient;
    private WebSocketSession webSocketSession;
    private BlockingQueue<String> receivedMessages;
    private StandardWebSocketClient wsClient;
    private ExecutorService executor;

    @BeforeEach
    void setUp() throws Exception {
        httpClient = HttpClient.newHttpClient();
        receivedMessages = new LinkedBlockingQueue<>();
        wsClient = new StandardWebSocketClient();
        executor = Executors.newSingleThreadExecutor();

        // Connetti il client WebSocket
        String wsUrl = "ws://localhost:" + port + "/holdupdates";
        CompletableFuture<WebSocketSession> sessionFuture = new CompletableFuture<>();
        
        executor.submit(() -> {
            try {
                WebSocketSession session = wsClient.execute(
                    new TextWebSocketHandler() {
                        @Override
                        public void afterConnectionEstablished(WebSocketSession session) {
                            System.out.println("WebSocket connesso");
                            sessionFuture.complete(session);
                        }

                        @Override
                        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                            String payload = message.getPayload();
                            System.out.println("WebSocket ricevuto: " + payload);
                            receivedMessages.add(payload);
                        }

                        @Override
                        public void handleTransportError(WebSocketSession session, Throwable exception) {
                            System.err.println("Errore WebSocket: " + exception.getMessage());
                            sessionFuture.completeExceptionally(exception);
                        }
                    },
                    null, new URI(wsUrl)
                ).get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                sessionFuture.completeExceptionally(e);
            }
        });

        webSocketSession = sessionFuture.get(10, TimeUnit.SECONDS);
        assertTrue(webSocketSession.isOpen(), "WebSocket deve essere connesso");
        
        // Aspetta un momento per stabilizzare la connessione
        Thread.sleep(500);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (webSocketSession != null && webSocketSession.isOpen()) {
            webSocketSession.close();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        receivedMessages.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Test completo: loadrequest → risposta backend → aggiornamento WebSocket")
    void testCompleteLoadRequestFlow() throws Exception {
        // ===== FASE 1: Ottieni lo stato iniziale =====
        System.out.println("\n=== FASE 1: Richiesta stato iniziale ===");
        
        // Mock della risposta dello stato iniziale
        IApplMessage mockInitialStateResponse = mock(IApplMessage.class);
        String initialStateContent = "holdstate('{\"MAXLOAD\":100,\"pids\":[1],\"names\":[\"box1\"],\"weights\":[30]}')";
        when(mockInitialStateResponse.msgContent()).thenReturn(initialStateContent);
        
        when(mockConnection.request(any(IApplMessage.class))).thenReturn(mockInitialStateResponse);

        String initialStateUrl = "http://localhost:" + port + "/holdstate";
        HttpRequest initialRequest = HttpRequest.newBuilder()
            .uri(URI.create(initialStateUrl))
            .GET()
            .build();

        HttpResponse<String> initialResponse = httpClient.send(
            initialRequest, 
            HttpResponse.BodyHandlers.ofString()
        );
        
        assertEquals(200, initialResponse.statusCode(), "Lo stato iniziale deve essere recuperato con successo");
        
        JSONObject initialState = new JSONObject(initialResponse.body());
        System.out.println("Stato iniziale: " + initialState.toString(2));
        
        assertEquals(100, initialState.getInt("maxload"));
        assertEquals(1, initialState.getJSONArray("pids").length());
        int initialWeight = initialState.getJSONArray("weights").getInt(0);
        
        // Verifica che il WebSocket abbia ricevuto lo stato iniziale
        String wsInitialMessage = receivedMessages.poll(3, TimeUnit.SECONDS);
        assertNotNull(wsInitialMessage, "Deve ricevere lo stato iniziale via WebSocket");
        JSONObject wsInitialState = new JSONObject(wsInitialMessage);
        assertEquals(100, wsInitialState.getInt("maxload"));

        // ===== FASE 2: Invia loadrequest =====
        System.out.println("\n=== FASE 2: Invio loadrequest ===");
        
        String testPid = "3";
        
        // Mock della risposta alla loadrequest
        IApplMessage mockLoadResponse = mock(IApplMessage.class);
        when(mockLoadResponse.msgContent()).thenReturn("loadaccepted(" + testPid + ")");
        
        verify(mockConnection, atLeastOnce()).request(any(IApplMessage.class));

        String loadRequestUrl = "http://localhost:" + port + "/loadrequest?pid=" + testPid;
        HttpRequest loadRequest = HttpRequest.newBuilder()
            .uri(URI.create(loadRequestUrl))
            .GET()
            .build();

        System.out.println("Invio loadrequest per PID: " + testPid);
        HttpResponse<String> loadResponse = httpClient.send(
            loadRequest,
            HttpResponse.BodyHandlers.ofString()
        );

        // Verifica la risposta HTTP
        assertEquals(200, loadResponse.statusCode(), "La loadrequest deve avere successo");
        String responseBody = loadResponse.body();
        System.out.println("Risposta loadrequest: " + responseBody);
        assertTrue(responseBody.contains("loadaccepted"), "La risposta deve indicare che il carico è stato accettato");
        assertTrue(responseBody.contains(testPid), "La risposta deve contenere il PID richiesto");

        // Verifica che sia stata fatta la richiesta corretta al backend
        verify(mockConnection, atLeastOnce()).request(any(IApplMessage.class));

        // ===== FASE 3: Simula aggiornamento CoAP che arriva dal backend =====
        System.out.println("\n=== FASE 3: Simulazione aggiornamento CoAP ===");
        
        // In un sistema reale, questo aggiornamento arriverebbe automaticamente
        // dal server CoAP quando lo stato dell'hold cambia.
        // Qui lo simuliamo manualmente per testare il flusso completo.
        
        String updatedStateJson = String.format(
            "{\"MAXLOAD\":100,\"pids\":[1,%s],\"names\":[\"box1\",\"box%s\"],\"weights\":[%d,25]}",
            testPid, testPid, initialWeight
        );
        
        // Simula l'arrivo dell'aggiornamento CoAP
        System.out.println("Simulazione invio aggiornamento CoAP...");
        Thread.sleep(100); // Simula un piccolo ritardo come nel sistema reale
        
        // Il CoapToWS parserebbe il messaggio e lo invierebbe via WebSocket
        JSONObject parsedUpdate = new JSONObject();
        parsedUpdate.put("maxload", 100);
        parsedUpdate.put("pids", new int[]{1, Integer.parseInt(testPid)});
        parsedUpdate.put("names", new String[]{"box1", "box" + testPid});
        parsedUpdate.put("weights", new int[]{initialWeight, 25});
        
        wsHandler.sendToAll(parsedUpdate.toString());

        // ===== FASE 4: Verifica che l'aggiornamento sia arrivato via WebSocket =====
        System.out.println("\n=== FASE 4: Verifica aggiornamento WebSocket ===");
        
        String updateMessage = receivedMessages.poll(5, TimeUnit.SECONDS);
        assertNotNull(updateMessage, "Deve ricevere l'aggiornamento via WebSocket dopo la loadrequest");
        
        JSONObject update = new JSONObject(updateMessage);
        System.out.println("Aggiornamento ricevuto: " + update.toString(2));
        
        // Verifica il contenuto dell'aggiornamento
        assertTrue(update.has("pids"), "L'aggiornamento deve contenere l'array pids");
        assertTrue(update.has("names"), "L'aggiornamento deve contenere l'array names");
        assertTrue(update.has("weights"), "L'aggiornamento deve contenere l'array weights");
        assertTrue(update.has("maxload"), "L'aggiornamento deve contenere maxload");
        
        // Verifica che il nuovo PID sia stato aggiunto
        assertEquals(2, update.getJSONArray("pids").length(), 
            "Dopo la loadrequest dovrebbero esserci 2 elementi");
        
        // Verifica che il PID richiesto sia presente
        boolean pidFound = false;
        for (int i = 0; i < update.getJSONArray("pids").length(); i++) {
            if (update.getJSONArray("pids").getInt(i) == Integer.parseInt(testPid)) {
                pidFound = true;
                break;
            }
        }
        assertTrue(pidFound, "Il PID " + testPid + " deve essere presente nell'aggiornamento");

        // ===== FASE 5: Verifica stato finale via REST =====
        System.out.println("\n=== FASE 5: Verifica stato finale ===");
        
        // Mock della risposta dello stato finale
        IApplMessage mockFinalStateResponse = mock(IApplMessage.class);
        when(mockFinalStateResponse.msgContent()).thenReturn(
            "holdstate('" + updatedStateJson + "')"
        );
        
        reset(mockConnection); // Reset per non interferire con le verify precedenti
        when(mockConnection.request(any(IApplMessage.class))).thenReturn(mockFinalStateResponse);

        HttpResponse<String> finalResponse = httpClient.send(
            initialRequest,
            HttpResponse.BodyHandlers.ofString()
        );
        
        JSONObject finalState = new JSONObject(finalResponse.body());
        System.out.println("Stato finale: " + finalState.toString(2));
        
        // Verifica che lo stato finale sia coerente con l'aggiornamento WebSocket
        assertEquals(
            update.getJSONArray("pids").length(),
            finalState.getJSONArray("pids").length(),
            "Lo stato REST e l'aggiornamento WebSocket devono essere coerenti"
        );
        
        System.out.println("\n✅ Test completato con successo!");
    }
}