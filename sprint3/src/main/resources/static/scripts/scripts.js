let robotMoving = false;
let robotPosition = 30;
let robotDirection = 1;
let animationFrame = null;

export function disconnectWebSocket() {
  document.getElementById("connectionStatus").textContent = "⚠️ Disconnesso";
  document.getElementById("connectionStatus").className =
    "connection-status disconnected";
}

export function connectWebSocket() {
  document.getElementById("connectionStatus").textContent = "✅ Connesso";
  document.getElementById("connectionStatus").className =
    "connection-status connected";
}

//Create slots
function createSlots(jsonobj) {
  const container = document.getElementById("slotsContainer");
  container.innerHTML = "";

  for (let i = 1; i <= 4; i++) {
    const slot = document.createElement("div");
    slot.className = "slot empty";
    slot.id = `slot${i}`;
    slot.innerHTML = `
                    <div class="slot-header">SLOT ${i}</div>
                    <div class="slot-field"><strong>PID:</strong> <span id="pid${i}">-</span></div>
                    <div class="slot-field"><strong>nome:</strong> <span id="name${i}">-</span></div>
                    <div class="slot-field"><strong>peso:</strong> <span id="weight${i}">0</span> kg</div>
                `;
    container.appendChild(slot);
  }
}

// Update UI with WebSocket data
export function updateUI(jsonobj) {
  let totalWeight = 0;
  let occupiedSlots = 0;

  for (let i = 0; i < jsonobj.pids.length; i++) {
    const slotElement = document.getElementById(`slot${i + 1}`);
    const pid = jsonobj.pids[i];
    const name = jsonobj.names[i];
    const weight = jsonobj.weights[i];

    // Update slot data
    document.getElementById(`pid${i + 1}`).textContent = pid || "-";
    document.getElementById(`name${i + 1}`).textContent = name || "-";
    document.getElementById(`weight${i + 1}`).textContent = weight || 0;

    // Update slot status
    if (pid && pid !== 0 && pid !== "") {
      slotElement.className = "slot occupied";
      occupiedSlots++;
      totalWeight += weight || 0;
    } else {
      slotElement.className = "slot empty";
    }
  }

  // Update weight info
  document.getElementById("currentWeight").textContent = totalWeight;
  document.getElementById("maxWeight").textContent = jsonobj.maxload || 0;
}

// Robot animation
function moveRobot() {
  if (!robotMoving) return;

  const robot = document.querySelector(".robot");
  const robotImg = document.getElementById("robotImg");
  const maxPosition = 1200;

  robotPosition += robotDirection * 2;

  if (robotPosition >= maxPosition) {
    robotDirection = -1;
    robotImg.style.transform = "scaleX(-1)"; // Specchia l'immagine
  } else if (robotPosition <= 30) {
    robotDirection = 1;
    robotImg.style.transform = "scaleX(1)"; // Immagine normale
  }

  robot.style.left = robotPosition + "px";
  animationFrame = requestAnimationFrame(moveRobot);
}

export function startRobot() {
  if (!robotMoving) {
    robotMoving = true;
    moveRobot();
  }
}

export function stopRobot() {
  robotMoving = false;
  if (animationFrame) {
    cancelAnimationFrame(animationFrame);
  }
}

//Send load requests
document
  .getElementById("sendRequestBtn")
  .addEventListener("click", function (e) {
    e.preventDefault();

    const pid = document.getElementById("pidfield").value.trim();
    const infoBox = document.getElementById("infobox");

    if (!pid) {
      document.getElementById("infobox").innerHTML =
        "<span class='error'>Inserire PID</span>";
      return;
    }

    fetch(`/loadrequest?pid=${encodeURIComponent(pid)}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("Errore HTTP: " + response.status);
        }
        return response.text();
      })
      .then((data) => {
        infoBox.innerHTML = data;
      })
      .catch((error) => {
        infoBox.innerHTML = `<span class="error">Errore: ${error.message}</span>`;
      });
  });

// Initialize
createSlots();

function fetchInitalState() {
  fetch(`/holdstate`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Errore HTTP: " + response.status);
      }
      return response.text();
    })
    .then((data) => {
      updateUI(JSON.parse(data));
    })
    .catch((error) => {
      infoBox.innerHTML = `<span class="error">Errore: ${error.message}</span>`;
    });
}

fetchInitalState();
//const interval = setInterval(fetchInitalState, 2000);

// Funzioni di simulazione eventi sonar con esportazione
function simulateSonarDetection() {
  fetch(`/sonardetection`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Errore HTTP: " + response.status);
      }
      return response.text();
    })
    .then((data) => {
      // updateUI(JSON.parse(data));
    })
    .catch((error) => {
      infoBox.innerHTML = `<span class="error">Errore: ${error.message}</span>`;
    });
}

function simulateSonarError() {
  fetch(`/sonarerror`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Errore HTTP: " + response.status);
      }
      return response.text();
    })
    .then((data) => {
      // updateUI(JSON.parse(data));
    })
    .catch((error) => {
      infoBox.innerHTML = `<span class="error">Errore: ${error.message}</span>`;
    });
}

function simulateSonarOk() {
  fetch(`/sonarok`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Errore HTTP: " + response.status);
      }
      return response.text();
    })
    .then((data) => {
      // updateUI(JSON.parse(data));
    })
    .catch((error) => {
      infoBox.innerHTML = `<span class="error">Errore: ${error.message}</span>`;
    });
}

window.simulateSonarDetection = simulateSonarDetection;
window.simulateSonarError = simulateSonarError;
window.simulateSonarOk = simulateSonarOk;
