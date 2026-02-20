// wsminimal.js - struttura esatta come indicato dal prof
// "Lo script wsminimal.js" - resource/static/wsminimal.js
//
// L'URL della WebSocket è costruito dinamicamente:
//   "ws://" + host + pathname + "socket"
// -> ws://localhost:8085/socket
//    (corrisponde al path registrato in WebSocketConfig)

var socket = connect();

function connect() {
   // var host     = document.location.host;
   // var pathname = document.location.pathname;
    // Il prof costruisce l'addr così (da wsminimal.js nel documento)
    //var addr     = "ws://" + host + pathname + "socket";
	var addr = "ws://" + document.location.host + "/socket";
    // Assicura che sia aperta una sola connessione
    if (socket !== undefined && socket.readyState !== WebSocket.CLOSED) {
        console.warn("WARNING: Connessione WebSocket già stabilita");
        return socket;
    }

    console.log("[wsminimal] Connessione a: " + addr);
    var ws = new WebSocket(addr);

    ws.onopen = function () {
        console.log("[wsminimal] Connessione WebSocket aperta");
        updateConnectionStatus(true);
    };

    ws.onmessage = function (event) {
        var data = event.data;
        // Prova a parsare come JSON (aggiornamento stato stiva)
        try {
            var state = JSON.parse(data);
            updateHoldUI(state);
        } catch (e) {
            // Messaggio testuale semplice (echo, log)
            appendToOutput(data);
        }
    };

    ws.onclose = function () {
        console.log("[wsminimal] Connessione WebSocket chiusa");
        updateConnectionStatus(false);
        // Riconnessione automatica dopo 3 secondi
        setTimeout(function () { socket = connect(); }, 3000);
    };

    ws.onerror = function (err) {
        console.error("[wsminimal] Errore WebSocket", err);
        updateConnectionStatus(false);
    };

    return ws;
}

function sendMessage() {
    var input = document.getElementById("inputmessage");
    if (!input) return;
    var msg = input.value;
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(msg);
        input.value = "";
    } else {
        alert("WebSocket non connessa!");
    }
}

// ── Aggiornamento UI ──────────────────────────────────────────

function updateConnectionStatus(connected) {
    var dot   = document.getElementById("conn-dot");
    var label = document.getElementById("conn-label");
    if (!dot) return;
    dot.className   = "conn-dot " + (connected ? "connected" : "error");
    label.textContent = connected ? "CONNESSO" : "DISCONNESSO";
}

function appendToOutput(text) {
    var area = document.getElementById("messageArea");
    if (!area) return;
    area.value += text + "\n";
    area.scrollTop = area.scrollHeight;
}

function updateHoldUI(state) {
    if (!state || !state.slots) return;

    // Aggiorna slot 1-4 (slot 5 è sempre occupato, non viene toccato)
    for (var i = 1; i <= 4; i++) {
        var card  = document.getElementById("slot-" + i);
        var label = document.getElementById("slot-" + i + "-label");
        if (!card) continue;
        var occ = state.slots[i] || false;
        card.classList.toggle("occupied", occ);
        label.textContent = occ ? "OCCUPATO" : "LIBERO";
    }

    // Aggiorna sensori
    var sonarEl = document.getElementById("sensor-sonar");
    if (sonarEl) {
        sonarEl.textContent = state.sonarStatus || "DFREE";
        sonarEl.className = "sensor-value " +
            (state.sonarStatus === "Container_Individuato" ? "warn" :
             state.sonarStatus === "ERRORE" ? "error" : "ok");
    }

    var ledEl = document.getElementById("sensor-led");
    if (ledEl) {
        ledEl.textContent = state.ledOn ? "ACCESO" : "SPENTO";
        ledEl.className = "sensor-value " + (state.ledOn ? "error" : "off");
    }

    var robotEl = document.getElementById("sensor-robot");
    var alarm   = document.getElementById("alarm-banner");
    if (robotEl) {
        robotEl.textContent = state.alarmActive ? "FERMO" : "OPERATIVO";
        robotEl.className = "sensor-value " + (state.alarmActive ? "error" : "ok");
    }
    if (alarm) alarm.classList.toggle("active", !!state.alarmActive);

    // Stats
    // Slot 5 (IO Port) è sempre occupato → +1 fisso al conteggio
    var occupied = Object.values(state.slots).filter(Boolean).length + 1;
    var statEl = document.getElementById("stat-occupied");
    var barEl  = document.getElementById("stat-bar");
    if (statEl) statEl.textContent = occupied + " / 5";
    if (barEl)  barEl.style.width = (occupied / 5 * 100) + "%";

    var updEl = document.getElementById("stat-update");
    if (updEl && state.lastUpdate) {
        var d = new Date(state.lastUpdate);
        updEl.textContent = d.toLocaleTimeString("it-IT");
    }

    // Log
    addLog(JSON.stringify(state.slots));
}

function addLog(msg) {
    var list = document.getElementById("log-list");
    if (!list) return;
    var entry = document.createElement("div");
    entry.className = "log-entry";
    var now  = new Date().toTimeString().slice(0, 8);
    entry.innerHTML =
        '<span class="log-time">' + now + '</span>' +
        '<span class="log-source">[ws]</span>' +
        '<span class="log-msg">' + msg + '</span>';
    list.prepend(entry);
    while (list.children.length > 50) list.removeChild(list.lastChild);
}

// Bind pulsante Send
document.addEventListener("DOMContentLoaded", function () {
    var btn = document.getElementById("sendMessage");
    if (btn) btn.addEventListener("click", sendMessage);
});