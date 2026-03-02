# Sprint3
- [Introduzione](#Introduzione)
- [Analisi del problema](#Analisi-del-problema)
- [Architettura Logica](#Architettura-Logica)
- [Progettazione](#Progettazione)


## Introduzione
Negli sprint precedenti è stato progettato e implementato il core business del sistema, basato su attori QAK cooperanti per la gestione del caricamento dei prodotti nella hold.
In particolare, il sistema è in grado di gestire richieste di carico, coordinare il robot e mantenere aggiornato lo stato della hold.

L’obiettivo dello Sprint 3 è introdurre un'interfaccia grafica web (WebGUI) che consenta all’utente finale di monitorare lo stato della hold, fornendo una visione chiara e aggiornata del sistema, senza interferire con la logica applicativa.

In questo sprint viene realizzata una base architetturale della WebGUI, sulla quale sarà possibile costruire e completare le funzionalità di integrazione.

### Requisiti

Il sistema deve soddisfare il seguente requisito: deve fornire un’interfaccia grafica web che consenta agli utenti di visualizzare lo stato della hold.

## Analisi del problema
### Modellazione
Dai requisiti emerge che la GUI deve essere:

- Accessibile via web
- Indipendente dal sistema core
- Orientata alla sola visualizzazione dello stato

Per tali motivi, la WebGUI non viene modellata come un attore, ma come una applicazione web separata, in grado di comunicare con il sistema esistente attraverso interfacce ben definite.

Si è scelto di adottare Spring Boot, framework Java per lo sviluppo di applicazioni web, in quanto coerente con le tecnologie già utilizzate, facilmente integrabile con il sistema esistente

### Estrarre le informazioni sullo stato della hold e visualizzazione
L'attore cargoservice, in esecuzione nel contesto ctx_cargo, contesto del corebusiness del sistema aggiorna costantemente lo stato della hold in seguito al caricamento prodotti. Per consentire alla GUI di visualizzare lo stato della hold, si possono esaminare molteplici modalità, come ad esempio:

- Implementare un'interfaccia RESTful nell'attore cargoservice per consentire alla GUI di effettuare richieste HTTP e ottenere lo stato della hold in formato JSON.
- Utilizzare un sistema di messaggistica (ad esempio MQTT o WebSocket) per inviare aggiornamenti in tempo reale da cargoservice alla GUI ogni volta che lo stato della hold cambia.
- Implementare un meccanismo di polling nella GUI che richieda periodicamente lo stato della hold all'attore cargoservice. 
- Prevedere l' aggiornamento di una risorsa CoAP da parte di cargoservice e recuperarne lo stato ad ogni cambiamento.

Si suggerisce di adottare l'ultima soluzione, in quanto coerente con le tecnologie già adottate nel sistema e con i paradigmi di comunicazione utilizzati, inoltre in fase di progettazione dell'attore qak cargoservice è già stato previsto l'aggiornamento di una risorsa CoAP, che contiene la descrizione dello stato della hold in formato JSON. 

## Architettura Logica
[immagine da mettere qua]

## Piano di Test
La WebGUI dispone di una suite di test che copre tutti i componenti principali:

- Data model (`HoldState`): Inizializzazione, update di slot occupati, tracciamento dei pesi.
- CoAP parsing (`CoapObserverService`): Parsing del payload per cambiamenti degli slot, stato del sonar, stato del LED, allarmi, eventi sui pesi.
- State Management (`HoldStateService`): Propagazione dello stato e meccanismi di broadcast.
- REST API (`SimulateController`): Simulazione di eventi per il testing senza una vera connessione CoAP.
- WebSocket Handler: Gestione connessioni e instradamento dei messaggi.
- Flusso end-to-end: Scenario multi-eventi che valida il flusso completo (parsing -> update -> broadcast).

Ulteriori dettagli disponibili nel file [`WebguiApplicationTests.java`]('./webgui/src/test/WebguiApplicationTest.java').

## Progettazione
La WebGUI segue un'architettura SpringBoot con chiare separazioni di responsabilità tra i seguenti layer:![i](../../sprint2/sprint2arch.png)

1. Modello dati (`HoldState`): Rappresentazione immutabile dello stato della stiva, stato del sensore, led e metriche sui pesi.
2. Servizio (`HoldStateService`): Coordinatore centrale che mantiene lo stato e orchestra il broadcast ai client connessi.
3. Origine eventi (`CoapObserverService`): Osserva le risorse CoAP dal backend qak usando Eclipse Californium. Estrae eventi strutturati eseguendo del parsing sui payload. Grazie al metodo `parseAndDispatch()` la GUI e le specifiche CoAP sono disaccoppiate, consentendo facilmente simulazioni e testing.
4. Comunicazione (`WebSockerHandlerDemo`, `WebSocketConfig`): Gestione connessioni websocket e propagazione in tempo reale dello stato. Mantiene una lista delle sessioni attive e manda in broadcast lo stato serializzato in JSON a tutti i client connessi (quando lo stato cambia).
5. REST API (`SimulateController`): Fornisce endpoint HTTP per i test ed integrazioni esterne senza richiedere una connessione CoAP in backend.
6. User Interface (`HIControllerDemo`, `index.html`): Interfaccia grafica aggiornata in tempo reale. 