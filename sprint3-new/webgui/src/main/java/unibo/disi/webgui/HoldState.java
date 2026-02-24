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
    // Formato reale:
    //   hold_state(slots:1=false;2=false;3=true;4=false,maxload:1000,weight:0,sonar:DFREE,led:Spento,robot:ok)
    private static final Pattern SNAP_SLOTS_BLOCK = Pattern.compile("slots:([\\d=a-zA-Z;]+)");
    private static final Pattern SNAP_SLOT_ENTRY  = Pattern.compile("(\\d+)=(true|false)");
    private static final Pattern SNAP_MAXLOAD     = Pattern.compile("maxload:(\\d+)");
    private static final Pattern SNAP_WEIGHT      = Pattern.compile("weight:(\\d+)");
    private static final Pattern SNAP_SONAR       = Pattern.compile("sonar:(\\w+)");
    private static final Pattern SNAP_LED         = Pattern.compile("led:(\\w+)");
    private static final Pattern SNAP_ROBOT       = Pattern.compile("robot:(\\w+)");
    private static final Pattern SNAP_ALARM       = Pattern.compile("alarm:(\\w+)");

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
     * Formato atteso:
     *   slots:1=false;2=false;3=true;4=false,maxload:1000,weight:0,sonar:DFREE,led:Spento,robot:ok
     * Ogni campo è opzionale: se assente viene lasciato al valore corrente.
     */
    public void applySnapshot(String snapshotText) {
        Matcher m;

        // slots:1=false;2=true;3=false;4=false
        m = SNAP_SLOTS_BLOCK.matcher(snapshotText);
        if (m.find()) {
            String block = m.group(1);          // es. "1=false;2=false;3=true;4=false"
            Matcher entry = SNAP_SLOT_ENTRY.matcher(block);
            while (entry.find()) {
                int id   = Integer.parseInt(entry.group(1));
                boolean occ = Boolean.parseBoolean(entry.group(2));
                slots.put(id, occ);
            }
        }

        m = SNAP_MAXLOAD.matcher(snapshotText);
        if (m.find()) maxLoad = Integer.parseInt(m.group(1));

        m = SNAP_WEIGHT.matcher(snapshotText);
        if (m.find()) currentWeight = Integer.parseInt(m.group(1));

        m = SNAP_SONAR.matcher(snapshotText);
        if (m.find()) sonarStatus = m.group(1);

        m = SNAP_LED.matcher(snapshotText);
        if (m.find()) ledOn = "acceso".equalsIgnoreCase(m.group(1)) || "true".equalsIgnoreCase(m.group(1));

        m = SNAP_ALARM.matcher(snapshotText);
        if (m.find()) alarmActive = "true".equalsIgnoreCase(m.group(1)) || "active".equalsIgnoreCase(m.group(1));

        // robot:ok — campo informativo, loggato ma non ancora modellato in HoldState
        m = SNAP_ROBOT.matcher(snapshotText);
        if (m.find()) {
            String robotStatus = m.group(1);
            java.util.logging.Logger.getLogger(HoldState.class.getName())
                .info("[HoldState] robot status da snapshot: " + robotStatus);
        }

        lastUpdate = LocalDateTime.now().toString();
    }
}