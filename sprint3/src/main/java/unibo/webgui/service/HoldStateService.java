package unibo.webgui.service;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;
import unibo.webgui.utils.HoldResponseParser;
import unibo.webgui.ws.WSHandler;

@RestController
public class HoldStateService {

    @Autowired
    private WSHandler wsHandler;

    private Interaction conn;

    private synchronized boolean ensureConn() {
        if (conn != null) return true;
        String host = System.getenv("CORE_HOST");
        if (host == null || host.isEmpty()) host = "127.0.0.1";
        String port = System.getenv("CORE_PORT");
        if (port == null || port.isEmpty()) port = "8000";
        try {
            conn = ConnectionFactory.createClientSupport23(ProtocolType.tcp, host, port);
            CommUtils.outgreen("Connessione TCP creata correttamente (" + host + ":" + port + ")");
            try {
                IApplMessage test = CommUtils.buildEvent("webgui", "ping", "X");
                conn.forward(test);
                return true;
            } catch (Exception ex) {
                System.err.println("Connessione creata ma non valida: " + ex.getMessage());
                conn = null;
                return false;
            }
        } catch (Exception e) {
            System.err.println("Impossibile creare connessione TCP verso " + host + ":8000 -> " + e.getMessage());
            conn = null;
            return false;
        }
    }
    public HoldStateService() {
        try {
            // try to establish connection lazily
            if (!ensureConn()) {
                CommUtils.outred("HoldStateService: initial connection not available");
            }
        } catch (Exception e) {
            System.err.println("Errore nella connessione TCP iniziale: " + e.getMessage());
        }
    }

    @GetMapping("/holdstate")
    public String getHoldState() {
        // try {
        //     if (!ensureConn()) return "{\"error\":\"no-connection-to-core\"}";
            //IApplMessage request = CommUtils.buildRequest("webgui", "getholdstate", "getholdstate(X)", "cargoservice");
        //     IApplMessage response;
        //     try {
        //         response = conn.request(request);
        //     } catch (NullPointerException npe) {
        //         // try to reconnect once
        //         conn = null;
        //         if (!ensureConn()) return "{\"error\":\"no-connection-to-core\"}";
        //         response = conn.request(request);
        //     }
        //     CommUtils.outblue("hold-state query response:" + response.msgContent());
            
        //     String jsonString = response.msgContent().substring(
        //             "'holdstate(".length(), 
        //             response.msgContent().length() - 2
        //         );
            
        //     JSONObject payload = HoldResponseParser.parseHoldState(jsonString);
        //     if (payload != null) {
        //         wsHandler.sendToAll(payload.toString());
        //         return payload.toString();
        //     } else {
        //         return "{\"error\":\"payload nullo\"}";
        //     }
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     return "{\"error\":\"" + e.getMessage() + "\"}";
        // }
        return "0";
    }
    
    @GetMapping("/sonardetection")
    public void sonardetection() {
    	CommUtils.outblue("sent detection");
        try {
            if (!ensureConn()) {
                CommUtils.outred("sonardetection: no connection to core");
                return;
            }
            IApplMessage request = CommUtils.buildEvent("webgui", "doDeposit", "X");
            conn.forward(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @GetMapping("/sonarerror")
    public void sonarerror() {
        try {
            if (!ensureConn()) {
                CommUtils.outred("sonarerror: no connection to core");
                return;
            }
            IApplMessage request = CommUtils.buildEvent("webgui", "sonaralert", "X");
            conn.forward(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @GetMapping("/sonarok")
    public void sonarok() {
        try {
            if (!ensureConn()) {
                CommUtils.outred("sonarok: no connection to core");
                return;
            }
            IApplMessage request = CommUtils.buildEvent("webgui", "sonarok", "X");
            conn.forward(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
