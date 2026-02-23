package unibo.disi.webgui;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Pattern per il parsing dello snapshot hold_state(...)
    private static final Pattern SNAP_SLOT    = Pattern.compile("slot_(\\d+)=(true|false)");
    private static final Pattern SNAP_SONAR   = Pattern.compile("sonar=(\\w+)");
    private static final Pattern SNAP_LED     = Pattern.compile("led=(\\w+)");
    private static final Pattern SNAP_ALARM   = Pattern.compile("alarm=(true|false)");
    private static final Pattern SNAP_MAXLOAD = Pattern.compile("maxload=(\\d+)");
    private static final Pattern SNAP_WEIGHT  = Pattern.compile("weight=(\\d+)");

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

    /**
     * Applica uno snapshot completo della stiva ricevuto tramite hold_state(TESTO).
     * Il testo è nella forma:
     *   slot_1=true,slot_2=false,slot_3=false,slot_4=true,sonar=DFREE,led=Acceso,alarm=false,maxload=500,weight=120
     * Ogni campo è opzionale: se assente viene lasciato al valore corrente.
     */
    public void applySnapshot(String snapshotText) {
        Matcher m;

        m = SNAP_SLOT.matcher(snapshotText);
        while (m.find()) {
            int id  = Integer.parseInt(m.group(1));
            boolean occ = Boolean.parseBoolean(m.group(2));
            slots.put(id, occ);
        }

        m = SNAP_SONAR.matcher(snapshotText);
        if (m.find()) sonarStatus = m.group(1);

        m = SNAP_LED.matcher(snapshotText);
        if (m.find()) ledOn = "acceso".equalsIgnoreCase(m.group(1)) || "true".equalsIgnoreCase(m.group(1));

        m = SNAP_ALARM.matcher(snapshotText);
        if (m.find()) alarmActive = Boolean.parseBoolean(m.group(1));

        m = SNAP_MAXLOAD.matcher(snapshotText);
        if (m.find()) maxLoad = Integer.parseInt(m.group(1));

        m = SNAP_WEIGHT.matcher(snapshotText);
        if (m.find()) currentWeight = Integer.parseInt(m.group(1));

        lastUpdate = LocalDateTime.now().toString();
    }
}