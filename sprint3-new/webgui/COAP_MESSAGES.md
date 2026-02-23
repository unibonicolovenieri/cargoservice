# Messaggi CoAP gestiti dalla WebGUI

La WebGUI osserva via CoAP le risorse esposte da QAktors sulla porta **8000**.
Quando un attore esegue un `emit`, aggiorna la sua risorsa CoAP e Californium
notifica automaticamente il `CoapObserverService`.

---

## Risorse osservate

| Risorsa CoAP | Attore QAktors |
|---|---|
| `coap://<host>:8000/ctx_cargo/sonar_test` | cargoservice |
| `coap://<host>:8000/ctx_cargo/cargorobot` | cargorobot |

---

## Messaggi riconosciuti (payload)

### `slot_changed(ID, STATUS)`
Aggiorna lo stato di occupazione di uno slot nella stiva.

| Parametro | Tipo | Esempio |
|---|---|---|
| `ID` | intero (1-4) | `1`, `2`, `3`, `4` |
| `STATUS` | booleano | `true` (occupato), `false` (libero) |

```
slot_changed(1,true)
slot_changed(3,false)
```

---

### `led_changed(STATUS)`
Aggiorna lo stato del LED di segnalazione.

| Parametro | Valori accettati |
|---|---|
| `STATUS` | `Acceso` oppure `Spento` |

```
led_changed(Acceso)
led_changed(Spento)
```

---

### `sonar_changed(STATUS)`
Aggiorna lo stato del sensore sonar.

| Parametro | Tipo | Esempio |
|---|---|---|
| `STATUS` | stringa | `DFREE`, `DBUSY`, ... |

```
sonar_changed(DFREE)
sonar_changed(DBUSY)
```

---

### `alarm(X)`
Segnala l'attivazione di un allarme.

| Parametro | Tipo | Note |
|---|---|---|
| `X` | qualsiasi | il valore non viene usato, conta solo la presenza del messaggio |

```
alarm(active)
```

---

### `problem_solved`
Disattiva l'allarme attivo. Non ha parametri.

```
problem_solved
```

---

### `maxload(N)`
Imposta il carico massimo supportato dalla stiva (valore assoluto).

| Parametro | Tipo | Esempio |
|---|---|---|
| `N` | intero (kg) | `500` |

```
maxload(500)
```

---

### `weight(N)`
Aggiunge N kg al peso corrente sulla stiva (**incrementale** — si somma al valore esistente).

| Parametro | Tipo | Esempio |
|---|---|---|
| `N` | intero (kg) | `50` |

```
weight(50)
```
> ⚠️ Ogni chiamata **aggiunge** N al totale. Se il peso attuale è 100 e arriva `weight(50)`, diventa 150.

---

### `current_weight(N)`
Imposta il peso attuale sulla stiva (**assoluto** — sovrascrive il valore corrente).

| Parametro | Tipo | Esempio |
|---|---|---|
| `N` | intero (kg) | `120` |

```
current_weight(120)
```
> ✅ Ogni chiamata **sovrascrive** il totale. Se il peso attuale è 100 e arriva `current_weight(120)`, diventa 120.

---

## Differenza tra `weight` e `current_weight`

| Messaggio | Semantica | Effetto sul campo `currentWeight` |
|---|---|---|
| `weight(50)` | incrementale | `currentWeight += 50` |
| `current_weight(120)` | assoluto | `currentWeight = 120` |

---

## Test senza QAktors (endpoint REST)

Il `SimulateController` espone due endpoint per testare i messaggi senza
dover avere QAktors attivo:

### `POST /simulate`
Inietta direttamente un payload nel parser CoAP.

```bash
curl -X POST http://localhost:8085/simulate \
     -H "Content-Type: application/json" \
     -d '{"payload": "current_weight(120)"}'
```

```bash
curl -X POST http://localhost:8085/simulate \
     -H "Content-Type: application/json" \
     -d '{"payload": "maxload(500)"}'
```

```bash
curl -X POST http://localhost:8085/simulate \
     -H "Content-Type: application/json" \
     -d '{"payload": "slot_changed(2,true)"}'
```

### `GET /state`
Restituisce lo stato JSON corrente della stiva.

```bash
curl http://localhost:8085/state
```

Risposta esempio:
```json
{
  "slots": { "1": false, "2": true, "3": false, "4": false },
  "sonarStatus": "DFREE",
  "ledOn": false,
  "alarmActive": false,
  "maxLoad": 500,
  "currentWeight": 120,
  "lastUpdate": "2026-02-20T10:30:00.123"
}
```
