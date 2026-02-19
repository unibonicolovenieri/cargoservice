package unibo.webgui.utils;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class HoldResponseParser {
	
    public static JSONObject parseHoldState(String message) {
        String jsonString = null;
		JSONObject payload = new JSONObject();
		
        // tolgo eventuali apici
		if (message.startsWith("'") && message.endsWith("'")) {
			jsonString = message.substring(1, message.length()-1);
		} 
		else if(message.startsWith("{")) {
			jsonString = message;
		}
		
		//Costruzione oggetti a partire da stringa JSON
		System.out.println(jsonString);
		JSONObject holdstate = new JSONObject(jsonString);
		
		int maxload = holdstate.getInt("MAXLOAD");
		JSONArray pidsJSON = holdstate.getJSONArray("pids");
		JSONArray namesJSON = holdstate.getJSONArray("names");
		JSONArray weightsJSON = holdstate.getJSONArray("weights");

		List<Integer> pids = new ArrayList<>();
		List<String> names = new ArrayList<>();
		List<Integer> weights = new ArrayList<>();

		for (int i = 0; i < pidsJSON.length(); i++) {
		    pids.add(pidsJSON.getInt(i));
		}

		for (int i = 0; i < namesJSON.length(); i++) {
		    names.add(namesJSON.getString(i));
		}

		for (int i = 0; i < weightsJSON.length(); i++) {
		    weights.add(weightsJSON.getInt(i));
		}
		
		payload.put("maxload", maxload);
		payload.put("pids", pids);
		payload.put("names", names);
		payload.put("weights", weights);
            
		return payload;
    }
    
    public static JSONObject parseRobotState(String message) {
    	String jsonString = null;
		JSONObject payload = new JSONObject();
		
        // tolgo eventuali apici
		if (message.startsWith("'") && message.endsWith("'")) {
			jsonString = message.substring(1, message.length()-1);
		} 
		else if(message.startsWith("{")) {
			jsonString = message;
		}
		
		//Costruzione oggetti a partire da stringa JSON
		if(jsonString != null) {
			JSONObject robotstate = new JSONObject(jsonString);	
			payload.put("status", robotstate.getBoolean("status"));
		}
		
		return payload;
    }
}