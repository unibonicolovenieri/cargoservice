# Sprint1

## Indice

- [Sprint1](#sprint1)
  - [Indice](#indice)
  - [Obbiettivi](#obbiettivi)
  - [Analisi del problema](#analisi-del-problema)
    - [CargoService](#cargoservice)
    - [Cargorobot](#cargorobot)
    - [ProductService](#productservice)
    - [Messaggi tra componenti](#messaggi-tra-componenti)
      - [Cargoservice](#cargoservice-1)
      - [Cargorobot](#cargorobot-1)
      - [Basicrobot](#basicrobot)
    - [ProductService](#productservice-1)
  - [Piano di test](#piano-di-test)
  - [Elaborazione](#elaborazione)
  - [Recap](#recap)
  - [Divisione dei task](#divisione-dei-task)

## Obbiettivi
L'obbiettivo prefissato di questo sprint è quello di analizzare i requisiti dei componenti *cargoservice* e *cargorobot* e di ciò che sta dietro a questi. Analizzeremo il problema e affronteremo un'elaborazione di progetto. Definiremo quali sono le **interazioni** tra questi componenti e il resto del sistema, ovvero sia ciò che i componenti comunicheranno con l'esterno sia ciò che riusciranno a digerire. Al termine di questo sprint, verrà redatto un piano di test per verificare che i componenti funzionino come previsto.


I requisiti che implementeremo in questo sprint sono:

1. Un sistema in grado di **ricevere** una richiesta di carico, **accettarla** o **rifiutarla** in base a fattori che visualizzeremo nel prossimo punto (Analisi del problema). Qualora stia elaborando una richiesta, **metta in coda** le richieste che arrivano contemporaneamente in maniera da non perderne nessuna e poterle gestire singolarmente una volta terminata la precedente.

2. Un sistema che riesca ad effettuare un **carico completo**.
    - Verifica dopo essersi posizionato all'IO port la presenza di un container
    - Carico del container
    - Posizionamento e scarico del container nello slot corretto
    - Return to home

3. Un sistema che sia in grado interrompere ogni attività in caso di malfunzionamento. Ovvero in caso di guasti o problemi tecnici, il sistema deve essere in grado di **interrompere** ogni attività e, una volta risolti i problemi, **farle ripartire**.

## Analisi del problema
In questo Sprint ci siamo concentrati sull'analisi dei due componenti [*cargoservice*](#cargoservice) e *cargorobot*. Abbiamo analizzato il problema e le interazioni che questi componenti avranno con il resto del sistema.

### CargoService
Il cargoservice è il componente che si occuperà di gestire le richieste di carico e scarico dei container. Le richieste arriveranno da un componente esterno e il cargo service dovrà elaborarle in base a diversi fattori, come la disponibilità degli slot, il peso totale dei container e l'ordine di arrivo, per poi accettarle o rifiutarle. Verrà implementato come un **orchestrator**: si occuperà di coordinare le attività del cargorobot, di gestire le richieste di carico in base allo stato del led e infine di comunicare con la web-gui per permettere l'interazione e il controllo da parte del committente.


Il ciclo di funzionamento del cargoservice sarà il seguente:

1. Ricezione del **PID** del prodotto (container) all'interno di una richiesta di carico 
2. **Verifica del peso** tramite una richiesta al componente **productservice**. In questa richiesta viene inserito il PID del prodotto del quale si vuole conoscere il peso.
3. La risposta di productservice può essere di due tipi:
    - **PID non registrato (ERRORE)**: il PID inviato da cargoservice non è registrato nel sistema. Cargoservice propagherà l'errore al mittente della richiesta di carico (in questo caso non avendo ancora implementato la parte dove vengono generate le richieste di carico, il mittente sarà un mockup che simulerà questo comportamento) e si preparerà a soddisfare la prossima richiesta.
    - **Peso relativo al PID**: Restituisce il peso relativo al PID inviato in precedenza.
  
4. Una volta ottenuto il peso la procedura di carico viene eseguita sotto le seguenti condizioni:
- il carico attualmente ospitato sommato al carico dell'eventuale prodotto da caricare non deve la costante `MAXLOAD` (`CURRENTLOAD` + `PRODUCT_WEIGHT` <= `MAXLOAD`)
- uno dei 5 slot (4 disponibili) deve essere libero per poter ospitare il prodotto

In caso di mancanza di una delle due condizioni verrà segnalato il relativo errore.
5. Cargoservice richiede al cargorobot di eseguire la load specificando il PID del prodotto, delegando la decisione dello slot in cui posizionarlo al cargorobot.
Questo serve a dividere la logica di gestione della stiva dall'effettiva evasione del compito,
 se ad esempio il robot impiegasse meno tempo a caricare il prodotto sullo slot numero 1 piuttosto che in altri, 
 vogliamo che il cargoservice ne sia totalmente ignaro.
6. Il cargoservice attende che il cargorobot ritorni alla HOME (posizione 0,0 dell'hold)
7. cargoservice riceve in risposta lo slot in cui è stato caricato il prodotto dal cargorobot, aggiorna lo stato della stiva(peso,numero di slot liberi) ed è pronto per gestire nuove richieste.

### Cargorobot
Il cargorobot gestisce il DDRrobot e si interfaccia con il cargoservice al fine di eseguire le richieste che arrivano. Ha conoscenza percui della posizione degli slot e del loro stato oltre alle informazioni della stiva( dimensione, ostacoli, perimetro, posizionamento dell'IOport)

Il cargorobot dovrà condividere con il basicrobot la modellazione della stiva. Il basicrobot fornito dal committente possiede una sua modellazione dell'hold che consiste in un rettangolo di celle della dimensione del robot, gli ostacoli(i nostri slot), il posizionamento dell'IOport e il led.

![](../../images/grigliarobot.jpg)




 .... (mappa), mosse, 
CARGOROBOT SI GESTISCE DA SOLO CHE SLOT TRA I LIBERI SCEGLIERE

quali malfunzionamenti, disponibilità degli slot e peso totale (MAXLOAD)

### ProductService
Il productservice è un componente che viene gia fornito dal committente per la registrazione e la gestione dei prodotti all'interno di un relativo Database. Esso permette la registrazione, la cancellazione e la ricerca di prodotti tramite il loro PID. Ogni prodotto ha associato un peso che verrà utilizzato dal cargoservice per verificare che il carico totale non superi la costante MAXLOAD. **Prodotto** invece sono le entità che verranno gestite, essendo passive potrebbero essere implementate come **POJO**. Gli attributi di un prodotto sono:

- PID (Valore Intero identifiativo del prodotto, deve essere maggiore di 0)
- Peso (Valore Reale che rappresenta il peso del prodotto, deve essere maggiore di 0)
- Nome (Stringa)

Come detto in precedenza ProductService è un componente già fornito dal committente, pertanto non verrà implementato da noi, ma ci limiteremo ad utilizzarlo per le nostre esigenze. Le interazioni che avremo con questo componente sono analizzate nel prossimo punto.

### Messaggi tra componenti

#### Cargoservice
Da scrivere i comandi 
#### Cargorobot
Da scrivere
Da verificare
```
Request handle_load_operation : handle_load_operation(SLOT) //Start operazione di carico  
Reply load_operation_done : load_operation_done(OK) for handle_load_operation //Conferma avvenuto carico 
```

#### Basicrobot
(Messaggi gia presenti nell'attore fornito dal committente)
```
    Dispatch cmd       	: cmd(MOVE)         
    Dispatch end       	: end(ARG)         
    
    Request step       : step(TIME)	
    Reply stepdone     : stepdone(V)                 for step
    Reply stepfailed   : stepfailed(DURATION, CAUSE) for step

    Event  sonardata   : sonar( DISTANCE ) 	   
    Event obstacle     : obstacle(X) 
    Event info         : info(X)    

    Request  doplan     : doplan( PATH, STEPTIME )
    Reply doplandone    : doplandone( ARG )    for doplan
    Reply doplanfailed  : doplanfailed( ARG )  for doplan

    Dispatch setrobotstate: setpos(X,Y,D) //D =up|down!left|right

    Request engage        : engage(OWNER, STEPTIME)	
    Reply   engagedone    : engagedone(ARG)    for engage
    Reply   engagerefused : engagerefused(ARG) for engage

    Dispatch disengage    : disengage(ARG)

    Request checkowner    : checkowner(CALLER)
    Reply checkownerok    : checkownerok(ARG)      for checkowner
    Reply checkownerfailed: checkownerfailed(ARG)  for checkowner
    
    Event alarm           : alarm(X)
    Dispatch nextmove     : nextmove(M)
    Dispatch nomoremove   : nomoremove(M)
    
    Dispatch setdirection : dir( D )  //D =up|down!left|right

    Request moverobot    :  moverobot(TARGETX, TARGETY)  
    Reply moverobotdone  :  moverobotok(ARG)                    for moverobot
    Reply moverobotfailed:  moverobotfailed(PLANDONE, PLANTODO) for moverobot
     
    Request getrobotstate : getrobotstate(ARG)
    Reply robotstate      : robotstate(POS,DIR)  for getrobotstate

    Request getenvmap     : getenvmap(X)
    Reply   envmap        : envmap(MAP)  for getenvmap
```
### ProductService
(Messaggi già presenti nell'attore fornito dal committente)
```
  Request createProduct : product(String)                    
  Reply   createdProduct: productid(ID) for createProduct   
        
  Request deleteProduct  : product( ID ) 
  Reply   deletedProduct : product(String) for deleteProduct

  Request getProduct : product( ID )  
  Reply   getProductAnswer: product( JSonString ) for getProduct 
    
  Request getAllProducts : dummy( ID )
  Reply   getAllProductsAnswer: products(  String ) for getAllProducts 
```



## Piano di test

Abbiamo simulato tramite un mockup il funzionamento di alcune componenti del sistema che al momento non sono ancora state implementate. Tuttavia tramite i test non solo ci sarà permesso di testare correttamente il funzionamento del sistema, ma anche di poter simulare il comportamento di alcune componenti che ancora non sono state implementate.

Che cosa abbiamo simulato?
Led e Sonar li simuliamo
Web-gui non la consideriamo per il momento.
## Elaborazione

## Recap

## Divisione dei task