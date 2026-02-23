package unibo.disi.webgui;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stato corrente della stiva (hold).
 * Serializzato come JSON e inviato ai browser via WebSocket.
 */
public class HoldState {

    private Map<Integer, Boolean> slots = new ConcurrentHashMap<>();
    private String sonarStatus = "DFREE";
    private boolean ledOn = false;
    private boolean alarmActive = false;
    private String lastUpdate = LocalDateTime.now().toString();
    private int maxLoad = 500;  // default 500 se non comunicato dal server
    private int currentWeight = 0;

    public int getMaxLoad() { return maxLoad; }
    public void setMaxLoad(int maxLoad) { 
        this.maxLoad = maxLoad; 
        lastUpdate = LocalDateTime.now().toString(); 
    }

    public int getCurrentWeight() { return currentWeight; }
    public void addWeight(int weight) { 
        this.currentWeight += weight; 
        lastUpdate = LocalDateTime.now().toString(); 
    }
    public void setCurrentWeight(int weight) {
        this.currentWeight = weight;
        lastUpdate = LocalDateTime.now().toString();
    }

    public HoldState() {
        for (int i = 1; i <= 4; i++) slots.put(i, false);
    }

    public void updateSlot(int id, boolean occupied) {
        slots.put(id, occupied);
        lastUpdate = LocalDateTime.now().toString();
    }

    // ---- Getters & Setters ----
    public Map<Integer, Boolean> getSlots() { return slots; }
    public String getSonarStatus() { return sonarStatus; }
    public void setSonarStatus(String s) { sonarStatus = s; lastUpdate = LocalDateTime.now().toString(); }
    public boolean isLedOn() { return ledOn; }
    public void setLedOn(boolean v) { ledOn = v; lastUpdate = LocalDateTime.now().toString(); }
    public boolean isAlarmActive() { return alarmActive; }
    public void setAlarmActive(boolean v) { alarmActive = v; lastUpdate = LocalDateTime.now().toString(); }
    public String getLastUpdate() { return lastUpdate; }
}