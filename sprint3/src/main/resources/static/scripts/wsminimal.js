import {
  updateUI,
  connectWebSocket,
  disconnectWebSocket,
  startRobot,
  stopRobot,
} from "./scripts.js";

var socket;

function connect() {
  var protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
  var host = document.location.host;
  var pathname = "/holdupdates";

  var addr = protocol + host + pathname;

  // Assicura che sia aperta un unica connessione
  if (socket !== undefined && socket.readyState !== WebSocket.CLOSED) {
    alert("WARNING: Connessione WebSocket già stabilita");
  }
  socket = new WebSocket(addr);

  //Alla apertura della WS
  socket.onopen = function (event) {
    console.log("Connected to " + addr);
    connectWebSocket();
  };

  socket.onclose = function (event) {
    console.log("Disconnected from " + addr);
    disconnectWebSocket();
  };

  //Alla ricezione di un messaggio dalla WS
  socket.onmessage = function (event) {
    var msg = event.data;

    console.log("ws-message: " + msg);

    var jsonobj = JSON.parse(msg);
    if (jsonobj.status !== null && jsonobj.status !== undefined) {
      if (jsonobj.status) startRobot();
      else stopRobot();
    } else {
      updateUI(jsonobj);
    }
  };
}

connect();
