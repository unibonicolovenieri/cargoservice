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
In questo Sprint ci siamo concentrati sull'analisi dei due componenti [*cargoservice*](#cargoservice) e [*cargorobot*](#cargorobot). Abbiamo analizzato il problema e le interazioni che questi componenti avranno con il resto del sistema.

### CargoService
Il cargoservice è il componente che si occuperà di gestire le richieste di carico e scarico dei container. Le richieste arriveranno da un componente esterno e il cargo service dovrà elaborarle in base a diversi fattori, come la disponibilità degli slot, il peso totale dei container e l'ordine di arrivo, per poi accettarle o rifiutarle. Verrà implementato come un **orchestrator**: si occuperà di coordinare le attività del cargorobot, di gestire le richieste di carico in base allo stato del led e infine di comunicare con la web-gui per permettere l'interazione e il controllo da parte del committente. Il `cargoservice` riceve anche eventi dal `sonar` (mock del sensore) che simulano la presenza o assenza del container, gestendo guasti e ripristini. In caso di `sonar_error`, il `cargoservice` emette `stop` per sospendere le operazioni.


Il ciclo di funzionamento del cargoservice sarà il seguente:

1. Ricezione del **PID** del prodotto (container) all'interno di una richiesta di carico (da richiesta diretta o da attore esterno).
2. **Verifica del peso** tramite una richiesta al componente **productservice**. In questa richiesta viene inserito il PID del prodotto del quale si vuole conoscere il peso.
3. La risposta di productservice può essere di due tipi:
    - **PID non registrato (ERRORE)**: il PID inviato da cargoservice non è registrato nel sistema. Cargoservice propagherà l'errore al mittente della richiesta di carico (in questo caso non avendo ancora implementato la parte dove vengono generate le richieste di carico, il mittente sarà un mockup che simulerà questo comportamento) e si preparerà a soddisfare la prossima richiesta.
    - **Peso relativo al PID**: Restituisce il peso relativo al PID inviato in precedenza.
  
4. Una volta ottenuto il peso la procedura di carico viene eseguita sotto le seguenti condizioni:
- il carico attualmente ospitato sommato al carico dell'eventuale prodotto da caricare non deve la costante `MAXLOAD` (`CURRENTLOAD` + `PRODUCT_WEIGHT` <= `MAXLOAD`)
- uno dei 5 slot (4 disponibili) deve essere libero per poter ospitare il prodotto

Dunque la risposta che cargoservice darà alla richiesta di carico sarà:
- **RIFIUTO**: In caso di mancanza di una delle due condizioni sopra - Risposta di errore
- **ACCETTAZIONE**: Condizioni soddisfatte e nella risposta viene specificato lo slot in cui il prodotto dovrà essere caricato.
In caso di mancanza di una delle due condizioni verrà segnalato il relativo errore.
5. Cargoservice richiede al cargorobot di eseguire la load specificando il PID del prodotto, delegando la decisione dello slot in cui posizionarlo al cargorobot.
Questo serve a dividere la logica di gestione della stiva dall'effettiva evasione del compito,
 se ad esempio il robot impiegasse meno tempo a caricare il prodotto sullo slot numero 1 piuttosto che in altri, 
 vogliamo che il cargoservice ne sia totalmente ignaro.
6. Il cargoservice attende che il cargorobot ritorni alla HOME (posizione 0,0 dell'hold)
7. cargoservice riceve in risposta lo slot in cui è stato caricato il prodotto dal cargorobot, aggiorna lo stato della stiva(peso,numero di slot liberi) ed è pronto per gestire nuove richieste.

xc
Abbiamo deciso che Cargoservice avrà due compiti fondamentali, ovvero quello di gestire gli slot e quello di coordinare l'operazione di carico. Necessiterà dunque di Request e Reply sviluppate in questo modo

```
  Request slot_request : slot_request(WEIGHT) //Richiesta di carico di un prodotto
  Reply slot_accepted : slot_accepted(SLOT) for slot_request //Conferma accettazione carico con assegnazione slot
  Reply slot_refused : slot_refused(REASON) for slot_request //Rifiuto con motivazione
```
```
  Request handle_load_operation : handle_load_operation(SLOT) //Start operazione di carico  
  Reply load_operation_done : load_operation_done(OK) for handle_load_operation //Conferma avvenuto carico 
```

#### Considerazioni Aggiuntive
In caso di evento scatenato dal led (es. malfunzionamento, emergenza) il cargoservice deve interrompere ogni attività in corso e attendere ulteriori istruzioni. Per comunicare queste interruzzioni a cargorobot possiamo inoltrare gli eventi che in futuro svilupperemo sul componente led. Led in questo sprint sarà un mock. Il cargoservice scatenerà due eventi stop e resume in risposta agli eventi dell'attuale mockup Led.

### Cargorobot
Il cargorobot gestisce il DDRrobot e si interfaccia con il cargoservice al fine di eseguire le richieste che arrivano. Ha conoscenza percui della posizione degli slot e del loro stato oltre alle informazioni della stiva (dimensione, ostacoli, perimetro, posizionamento dell'IOport)

Il cargorobot dovrà condividere con il basicrobot la modellazione della stiva. Il basicrobot fornito dal committente possiede una sua modellazione dell'hold che consiste in un rettangolo di celle della dimensione del robot, gli ostacoli(i nostri slot), il posizionamento dell'IOport e il led.

Può gestire eventi `stop` e `resume` ricevuti dal `cargoservice`. Durante `stop` emette un `alarm` e sospende l'attività, la variabile `delivering` ci informa sullo stato. Su `resume`, se stava consegnando, riprende automaticamente dal punto in cui era rimasto.

![](../../images/grigliarobot.jpg)

L'attività che il cargorobot dovrà svolgere sarà la seguente:
1. Il cargorobot riceve da cargoservice una richiesta di gestione di un container e lo slot in cui posizionarlo.
3. Il cargorobot si dirige verso la pickup-position e preleva il container o attende che questo venga posizionato sull'IOport.
4. Succesivamente dopo aver prelevato il container, si dirige verso lo slot fornito in precedenza e deposita il container.
5. Una volta completata l'operazione cargorobot ritorna in (0,0) HOME e notifica a cargoservice il completamento dell'operazione. 

#### Considerazioni Aggiuntive

Il cargorobot deve tornare in HOME e solo dopo aver effettuato un tune_at_home notificare al cargoservice il completamento dell'operazione. 

In caso di evento scatenato dal led (es. malfunzionamento, emergenza) il cargorobot deve interrompere ogni attività in corso e attendere ulteriori istruzioni. 
Questo ci porta alla conclusione di dover gestire e mantenere memorizzate alcune informazioni:

- Avanzamento della richiesta (Arrivato all'IOport, Arrivato allo slot, Arrivato a HOME)
- Salvataggio della richiesta in corso (SLOT in cui effettuare il caricamento se non ancora eseguito)

Utilizzeremo *alarm(x)* per notificae basicrobot un evento di blocco. Useremo *nome comando* per riprendere l'attività.

### ProductService

Il productservice è un componente che viene gia fornito dal committente per la registrazione e la gestione dei prodotti all'interno di un relativo Database. Esso permette la registrazione, la cancellazione e la ricerca di prodotti tramite il loro PID. Ogni prodotto ha associato un peso che verrà utilizzato dal cargoservice per verificare che il carico totale non superi la costante MAXLOAD. **Prodotto** invece sono le entità che verranno gestite, essendo passive potrebbero essere implementate come **POJO**. Gli attributi di un prodotto sono:

- PID (Valore Intero identifiativo del prodotto, deve essere maggiore di 0)
- Peso (Valore Reale che rappresenta il peso del prodotto, deve essere maggiore di 0)
- Nome (Stringa)

Come detto in precedenza ProductService è un componente già fornito dal committente, pertanto non verrà implementato da noi, ma ci limiteremo ad utilizzarlo per le nostre esigenze. Le interazioni che avremo con questo componente sono analizzate nel prossimo punto.

## Messaggi tra componenti

### Contesti
```
ctx_cargo (localhost:8000)          [CORE DEL SISTEMA]
├── cargorobot
├── cargoservice
├── test
└── sonar_test

ctx_basicrobot (127.0.0.1:8020)     [ROBOT FISICO]
└── basicrobot (ExternalQActor)

ctx_cargoservice (127.0.0.1:8111)   [SERVIZIO PRODOTTI]
└── productservice (ExternalQActor)
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
#### ProductService
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
#### Messaggi nuovi
```
  Request load_product : product(ID)
  Reply   loadedProduct : slot(SLOT) for load_product

  Request move_product : product(SLOT)
  Reply   movedProduct : result(SLOT) for move_product
  Reply   moveProductFailed : fail(failed) for move_product

  Event stop : stop(X)
  Event resume : resume(X)
  Event alarm : alarm(X)

  Event container_trigger : container_trigger(X)
  Event container_absence : container_absence(X)
  Event sonar_error : sonar_error(CAUSA)
  Event problem_solved : problem_solved(CAUSA)

```


## Piano di test

Abbiamo simulato tramite un mockup il funzionamento di alcune componenti del sistema che al momento non sono ancora state implementate. Tuttavia tramite i test non solo ci sarà permesso di testare correttamente il funzionamento del sistema, ma anche di poter simulare il comportamento di alcune componenti che ancora non sono state implementate.

Nella fase di test, viene mandata una richiesta dal componente di Test e gestita da `cargoservice`.

Il flusso di lavoro è il seguente:
```
test actor:
  createProduct
  |
  createdProduct
  |
  request load_product al cargoservice
  |
  cargoservice chiama productservice
  |
  assegna slot
  |
  invia move_product a cargorobot
  |
  cargorobot completa e notifica
  |
  cargoservice aggiorna stato
```

Aggiunto: attore `sonar_test` che simula il comportamento del sensore sonar, gestendo ciclicamente eventi di:
- arrivo container (`container_trigger`)
- assenza (`container_absence`)
- guasti (`sonar_error`)
- ripristino (`problem_solved`)
Lo scopo è testare la capacità del sistema di interrompere e riprendere le attività automaticamente.

```
sonar_test -->  cargoservice -->  cargorobot -->  basicrobot  
      ↑               ↓
problem_solved   stop/resume
```

## Elaborazione

È stata gestita la sincronizzazione tra `cargoservice` e `cargorobot` in presenza di errori del sonar, con l’introduzione degli eventi `stop`, `resume`, `alarm`, `sonar_error` e `problem_solved`.
Il test `sonar_test` permette di validare il comportamento del sistema in caso di errori temporanei.

## Recap

## Divisione dei task

Abbiamo impiegato in totale 20 ore di lavoro per completare questo sprint, suddivise tra le varie attività come segue:
- Analisi del problema: 8 ore
- Redazione del documento: 6 ore
- Pianificazione e Sviluppo del test: 6 ore

