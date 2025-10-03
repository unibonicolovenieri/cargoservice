# Sprint1

## Indice

- [Sprint1](#sprint1)
  - [Indice](#indice)
  - [Obbiettivi](#obbiettivi)
  - [Analisi del problema](#analisi-del-problema)
    - [CargoService](#cargoservice)
    - [Cargorobot](#cargorobot)
  - [Piano di test](#piano-di-test)
  - [Elaborazione](#elaborazione)
  - [Recap](#recap)
  - [Divisione dei task](#divisione-dei-task)

## Obbiettivi
L'obbiettivo prefissato di questo sprint è quello di analizzare i requisiti dei componenti *cargoservice* e *cargorobot* e di cio che sta dietro a questi. Analizzeremo il problema e afronteremo un elaborazione di progetto. Definiremo quali sono le **interazioni** tra questi componenti e il resto del sistema, ovvero sia ciò che comunicheranno con l'esterno sia ciò che riusciranno a digerire. Al termine infine di questo sprint, verrà redatto un piano di test per verificare che i componenti funzionino come previsto.


I requisiti che implementeremo in questo sprint sono:

1. Un sistema in grado di **ricevere** una richiesta di carico, **accettarla** o **rifiutarla** in base a fattori che visualizzeremo nel prossimo punto (Analisi del problema). Qual'ora stia elaborando una richiesta, **metta in coda** le richieste che arrivano contemporaneamente in maniera da non perderne nessuna e poterle gestire singolarmente una volta terminata la precedente.

2. Un sistema che riesca ad effettuare un **carico completo**.
    - Verifica dopo essersi posizionato all'IO port la presenza di un container
    - Carico del container
    - Posizionamento e scarico del container nello slot corretto
    - Return to home

3. Un Sistema che sia in grado interrompere ogni attività in caso di malfunzionamento. Ovvero in caso di guasti o problemi tecnici, il sistema deve essere in grado di **interrompere** ogni attività e, una volta risolti i problemi, **farle ripartire**.

## Analisi del problema
In questo Sprint ci siamo concentrati sull'analisi dei due componenti [*cargoservice*](#cargoservice) e *cargorobot*. Abbiamo analizzato il problema e le interazioni che questi componenti avranno con il resto del sistema.

### CargoService
Il cargoservice è il componente che si occuperà di gestire le richieste di carico e scarico dei container. Le richieste arriveranno da un componente esterno e il cargo service dovrà elaborarle e ,in base a diversi fattori, come la disponibilità degli slot, il peso totale dei container e l'ordine di arrivo per poi accettarle o rifiutarle. Verrà implementato come un **orchestrator** che si occuperà di coordinare le attività del cargorobot, di gestire le richieste di carico in base allo stato del led e infine di comunicare con la web-gui per permettere l'interazione e il controllo da parte del committente.


Il ciclo di funzionamento del cargoservice sarà il seguente:

1. Ricezione del **PID** del prodotto (container) all'interno di una richiesta di carico 
2. **Verifica del peso** tramite una richiesta al componente **productservice**. In questa richiestsa viene inserito il PID del prodotto del quale si vuole conoscere il peso.
3. La risposta di productservice può essere di due tipi:
    - **PID non registrato (ERRORE)**: il PID inviato da cargoservice non è registrato nel sistema. Cargoservice propagherà l'errore al mittente della richiesta di carico (in questo caso non avendo ancora implementato la parte dove vengono generate le richieste di carico, il mittente sarà un mockup che simulerà questo comportamento).
    - **Peso relativo al PID**: Restituisce il peso relativo al PID inviato in precedenza.
  
4. 

### Cargorobot
cosa sa fare
Il cargorobot dovrà condividere con il basicrobot la modellazione della stiva. Il basicrobot fornito dal committente possiede una sua modellazione dell'hold che consiste in un rettangolo di celle della dimensione del robot, gli ostacoli(i nostri slot) .... (mappa), mosse, 
CARGOROBOT SI GESTISCE DA SOLO CHE SLOT TRA I LIBERI SCEGLIERE

quali malfunzionamenti, disponibilità degli slot e peso totale (MAXLOAD)
## Piano di test

Abbiamo simulato tramite un mockup il funzionamento di alcune componenti del sistema che al momento non sono ancora state implementate. Tuttavia tramite i test non solo ci sarà permesso di testare correttamente il funzionamento del sistema, ma anche di poter simulare il comportamento di alcune componenti che ancora non sono state implementate.

Che cosa abbiamo simulato?
## Elaborazione

## Recap

## Divisione dei task