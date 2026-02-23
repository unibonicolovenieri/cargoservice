# Sprint3
- [Introduzione](#Introduzione)
- [Analisi del problema](#Analisi-del-problema)
- [Architettura Logica](#Architettura-Logica)
- [Progettazione](#Progettazione)


## Introduzione
Negli sprint precedenti è stato progettato e implementato il core business del sistema, basato su attori QAK cooperanti per la gestione del caricamento dei prodotti nella hold.
In particolare, il sistema è in grado di gestire richieste di carico, coordinare il robot e mantenere aggiornato lo stato della hold.

L’obiettivo dello Sprint 3 è introdurre una interfaccia grafica web (WebGUI) che consenta all’utente finale di monitorare lo stato della hold, fornendo una visione chiara e aggiornata del sistema, senza interferire con la logica applicativa.

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

- Implementare un'interfaccia RESTful nell'attore holdmanager per consentire alla GUI di effettuare richieste HTTP e ottenere lo stato della hold in formato JSON.
- Utilizzare un sistema di messaggistica (ad esempio MQTT o WebSocket) per inviare aggiornamenti in tempo reale dalla holdmanager alla GUI ogni volta che lo stato della hold cambia.
- Implementare un meccanismo di polling nella GUI che richieda periodicamente lo stato della hold all'attore holdmanager.
- Prevedere l' aggiornamento di una risorsa CoAP da parte di holdmanager e recuperarne lo stato ad ogni cambiamento.

Si suggerisce di adottare l'ultima soluzione, in quanto coerente con le tecnologie già adottate nel sistema e con i paradigmi di comunicazione utilizzati, inoltre in fase di progettazione dell'attore qak holdmanager è già stato previsto l'aggiornamento di una risorsa CoAP, che contiene la descrizione dello stato della hold in formato JSON. 

## Architettura Logica
...
## Progettazione
...

