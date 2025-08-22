# Sprint 0

## Indice

- [Obiettivi](#obiettivi) ✅
- [Analisi dei Requisiti](#analisi-dei-requisiti) ❌    
- [Macrocomponenti](#macrocomponenti) ❌
- [Architettura di Riferimento](#architettura-di-riferimento) ❌
- [Piano di Test](#piano-di-test) ❌
- [Piano di Lavoro](#piano-di-lavoro) ❌

## Obiettivi
In questo sprint0 i nostri obiettivi sono di analizzare e individuare sottoinsiemi di requisiti forniti dal committente e definire il nostro problema, per poi in futurio suddividere i sottoinsiemi in successivi sprint da eseguire eventualmente anche in parallelo, improntare le componenti della nostra archiettura (macrocomponenti principali & interazioni tra loro sotto forma di messaggi). Il sistema è distribuito?

## Analisi dei requisiti
- Tutti i requisiti analizzati fanno riferimento ai [requisiti del commitente](../requirements/README.md)

Si elencano le nomenclature utilizzate del problema
### Hold
È la stiva della nave, cioè l’area interna e piatta dove vengono caricati i container con i prodotti. In questo progetto è una zona rettangolare con degli slot e una porta di ingresso/uscita (IOPort).
### Cargorobot
È il robot a guida differenziale (Differential Drive Robot) incaricato di spostare i container dentro la stiva e piazzarli nello slot assegnato. Dopo il lavoro torna sempre alla sua posizione “HOME”.
### Products
Sono i beni/merci che devono essere caricati sulla nave. Ogni prodotto viene messo in un container di dimensioni prefissate e registrato in un sistema.
### Weight
Il peso del prodotto/container. Serve per verificare che non venga superato il limite massimo di carico della nave (`MaxLoad`).
### Productservice
È il servizio software che gestisce la registrazione dei prodotti. Quando inserisci un prodotto specificando il peso, lui restituisce un identificativo unico (`PID`).
### Io-port
È la porta di ingresso/uscita della stiva. Davanti a questa porta c’è un sensore sonar che rileva se un container è presente. È il punto dove il prodotto viene consegnato prima che il robot lo carichi.
### Slots & Sensors
- Slots: sono le aree (4 in totale) all’interno della stiva dove i container vengono sistemati. Uno slot è già occupato in modo permanente, gli altri inizialmente sono liberi.

- Sensors: in questo caso si parla di un sonar davanti all’IOPort che rileva la presenza dei container e segnala eventuali anomalie (tipo guasto se non misura più distanze corrette).

### DDR Differential Drive Robot
È il tipo di robot mobile con due ruote motrici indipendenti. Si muove facendo girare le ruote a velocità diverse (come i robot aspirapolvere), ed è quello usato come cargorobot.
### WENV
WENV è un ambiente di simulazione software (“Web Environment”) usato per testare il sistema, mostrare la stiva, lo stato degli slot e i movimenti del robot tramite un’interfaccia grafica web.


# Attori vs POJO (spiegare come modelleremo i vari componenti)
# QAK (?)
## Perchè lo utilizzeremo e quali principi utilizzeremo
## Link alla documentazione ufficiale Qak
# interazione del cargorobot (come parla, con che messaggi e cosa è capace di fare)
# come si muove il robot in hold
# sonar
# Componenti fornite dal committente
# gui
# test

## Piano di Test

## Piano di Lavoro
Successivi allo sprint0 si distinuguono i seguenti sprint operativi del nostro processo Scrum

1. Sprint 1
    - Cargoservice (core buisness del sistema)
    - Cargorobot
2. Sprint 2
    - Hold
3. Sprint 3
    - Sonar
    - Led
4. Sprint 4
    - Web Gui

Divisione Temporale e Data inizio e fine e Lavoro




## Gruppi di Requisiti
come dividere il lavoro? 
cargoservice
holddddddd()???
sonar
gui 
? 
alternative? se li dividessimo in 4 sprint cosa potremmo sviluppare in parallelo?
