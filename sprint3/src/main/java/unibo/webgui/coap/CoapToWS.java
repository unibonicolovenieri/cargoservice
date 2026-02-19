package unibo.webgui.coap;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;
import unibo.webgui.utils.HoldResponseParser;
import unibo.webgui.ws.WSHandler;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


@Component
public class CoapToWS {
//	Local development
//    private static final String COAP_ENDPOINT_HOLD = "coap://127.0.0.1:8999/ctx_cargo/holdmanager";
//    private static final String COAP_ENDPOINT_ROBOT = "coap://127.0.0.1:8999/ctx_cargo/cargorobot";

	//Docker images
    private static final String COAP_ENDPOINT_SONAR = "coap://127.0.0.1:7777/ctx_iodevices/sonar";  
    private static final String COAP_ENDPOINT_HOLD = "coap://127.0.0.1:8000/ctx_cargo";
    private static final String COAP_ENDPOINT_ROBOT = "coap://localhost:8000/ctx_cargo/cargorobot";
//	
    private CoapClient clienthold;
    private CoapClient clientrobot;
    private CoapObserveRelation observeRelationHold;
    private CoapObserveRelation observeRelationRobot;

    @Autowired
    private WSHandler wsHandler;
       
    @PostConstruct
    public void init() {	
    	
    	String coapHost;
    	
        // Risolvi l'hostname in IP
        try {
            java.net.InetAddress address = java.net.InetAddress.getByName("172.17.0.1");
            coapHost = address.getHostAddress();
            // coapHost = "localhost";
            System.out.println("Hostname sprint1 risolto in: " + coapHost);
        } catch (Exception e) {
            System.err.println("Errore risoluzione hostname sprint1: " + e.getMessage());
            // Fallback all'hostname originale o a localhost
            coapHost = "127.0.0.1";
        }
        
        clienthold = new CoapClient(COAP_ENDPOINT_HOLD);
        observeRelationHold = clienthold.observe(
        	new CoapHandler() {
	            @Override
	            public void onLoad(CoapResponse response) {
	                String content = response.getResponseText();
	                CommUtils.outblue("CoAP payload - hold: " + content);
	                if(content != null && !content.trim().isBlank() && !content.trim().equals("nonews")) {
	                	try {
		                    //JSONObject payload = HoldResponseParser.parseHoldState(content);
                            CommUtils.outred("Content: " + content);
		                    // if (payload != null) {
		                    //     wsHandler.sendToAll(payload.toString());
		                    // } else {
		                    //     CommUtils.outred("Evento CoAP non valido: " + content);
		                    // }
		                } catch (Exception e) {
		                    e.printStackTrace();
		                }
	                }
	            }
	
	            @Override
	            public void onError() {
	                System.err.println("Errore nell'osservazione CoAP su " + COAP_ENDPOINT_HOLD);
	            }
        });
        System.out.println("Iniziata osservazione CoAP su: " + COAP_ENDPOINT_HOLD);
        
    //     clientrobot = new CoapClient(COAP_ENDPOINT_ROBOT);
    //     observeRelationRobot = clientrobot.observe(
    //     	new CoapHandler() {
    //         @Override
    //         public void onLoad(CoapResponse response) {
    //             String content = response.getResponseText();
    //             CommUtils.outblue("CoAP payload - robot: " + content);

    //             if(content != null && !content.isBlank()) {
	//                 try {
	//                     JSONObject payload = HoldResponseParser.parseRobotState(content);
	//                     if (payload != null) {
	//                         wsHandler.sendToAll(payload.toString());
	//                     } else {
	//                         CommUtils.outred("Evento CoAP non valido: " + content);
	//                     }
	//                 } catch (Exception e) {
	//                     e.printStackTrace();
	//                 }
    //             }
    //         }

    //         @Override
    //         public void onError() {
    //             System.err.println("Errore nell'osservazione CoAP su " + COAP_ENDPOINT_ROBOT);
    //         }
    //     });
    //     System.out.println("Iniziata osservazione CoAP su: " + COAP_ENDPOINT_ROBOT);
     }
	
}