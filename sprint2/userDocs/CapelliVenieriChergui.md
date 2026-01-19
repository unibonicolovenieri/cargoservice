# Sprint 2

## Introduzione
In questo Sprint l'obbiettivo prefissato è quello di analizzare e comprendere le caratteristiche del componente Sensor. Si procederà poi con lo sviluppoo di un'implementazione funzionante che si andrà ad integrare con il restate core-buisness del sistema. 

## Requisiti
Vanno completati i seguenti requisiti:
- Implementazione di un sistema che attraverso un sensore, posizionato di fronte all'IO-PORT in grado di rilevare la presenza di un oggetto, sia in grado di comunicare al sistema che è presente un container. La rilevazione avviene quando il sonar misura una distanza **D tale che D < DFREE/2** per un tempo continuativo di almeno 3 secondi.
- Quando il sonar misura una distanza **D >= DFREE/2** per un tempo continuativo di almeno 3 secondi, il sistema deve interrompere le proprie attività e inoltre si deve scatenare l'accensione di un LED rosso (per comunicare un malfunzionamento del sonar).
- Nel momento in cui la distanza misurata torna ad essere **D <= DFREE** il sistema deve riprendere le proprie attività e il LED rosso deve spegnersi.
## Analisi del Problema
Analizzando i requisiti del problema, siamo in grado di identificare inanzitutto due componenti Hardware **Sonar** fisico e un **LED**. I quali saranno implementati su un sistema RaspberryPI.
### Modellazione del Sensore
Il problema iniziale che ci sorge è quello di poter permettere ai Componenti Hardware (E dei relativi componenti software) di poter comunicare ed interagire con il core-buisness del sistema. Per fare ciò, le soluzioni possibili sono due:
- Implementare sensor come **componente** che gestisca sonar e led modellati come classi **POJO**
- Implementare un **contesto (sonar)** che contiene al suo interno due **attori** **Sonar**  e **LED**.

La prima opzione può essere presa in considerazione per la comodità di avere un unico componente di gestione, risultà però essere meno flessibile in quanto non permette ai componenti di comunicare direttamente con il corebuisness e inoltre non rispetta il Single Responsability Principle. La seconda opzione invece permette la comunicazione con il corebuisness da parte dei singoli attori e inoltre mantiene valido il principio preceentemente citato. 
### Sonar
Il sonar si deve interfacciare con il core-buisness sarà quindi necessario comunicare ogni evento scatenato utile alla modifica dello stato del sistema ( E quindi non comunicare ogni singola misurazione effettuata).Le comunicazioni che il Sonar avrà con il resto del sistema sono state definite nello sprint precedente e sono: 

```
◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆
INSERIRE QUI GLI EVENTI CON RELATIVA SPIEGAZIONE
◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆
```

#### Componente Fisico
Per l'interazione tra il componente software e il componente hardware sfrutteremo il codice python fornito dal committente per interagire con il RaspberryPI.

``` python
# Sonar Controller
# File: sonar.py
import RPi.GPIO as GPIO
import time
import sys

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
TRIG = 17
ECHO = 27

GPIO.setup(TRIG, GPIO.OUT)
GPIO.setup(ECHO, GPIO.IN)
GPIO.output(TRIG, False)   # TRIG parte LOW

print ('Waiting a few seconds for the sensor to settle')
time.sleep(2)

while True:
  GPIO.output(TRIG, True)    #invia impulso TRIG
  time.sleep(0.00001)
  GPIO.output(TRIG, False)

  pulse_start = time.time()
  #attendi che ECHO parta e memorizza tempo
  while GPIO.input(ECHO)==0:
      pulse_start = time.time()
  # register the last timestamp at which the receiver detects the signal.
  while GPIO.input(ECHO)==1:
      pulse_end = time.time()
  pulse_duration = pulse_end - pulse_start

  # velocità del suono ~= 340m/s 
  # distanza = v*t 
  # il tempo ottenuto misura un roundtrip -> distanza = v*t/2
  distance = pulse_duration * 17165
  distance = round(distance, 1)
  print ('Distance:', distance,'cm')
  sys.stdout.flush()
  time.sleep(1)
```

### LED
Il led è un componente hardware che permette di segnalare visivamente lo stato del sistema. Abbiamo la necessità di rilevare lo stato del sonar per poi poter comandare lo stato del led (acceso/spento). Il led dunque, dovrà intercettare gli eventi inviati dal sonar a cargoservice (relativi al led) e accendersi o spegnersi in base a quest'ultimi.

#### Componente Fisico
Anche per il led sfrutteremo il codice python fornito dal committente per interagire con il RaspberryPI.

``` python
#ACCENSIONE LED
#file ledPython25On.py
import RPi.GPIO as GPIO 
import time

'''
----------------------------------
CONFIGURATION

'''
GPIO.setmode(GPIO.BCM)
GPIO.setup(25,GPIO.OUT)

'''
----------------------------------
main activity
----------------------------------
'''
GPIO.output(25,GPIO.HIGH)
```

``` python
#SPEGNIMENTO LED
#file ledPython25Off.py
# -------------------------------------------------------------
#
# -------------------------------------------------------------
import RPi.GPIO as GPIO 
import time

'''
----------------------------------
CONFIGURATION
----------------------------------
'''
GPIO.setmode(GPIO.BCM)
GPIO.setup(25,GPIO.OUT)

'''
----------------------------------
main activity
----------------------------------
'''

GPIO.output(25,GPIO.LOW)
```



## Architettura del Sistema

## Test

## Sviluppo del Progetto 
### SonarController Actor
### Sonar Actor
### LED Actor
## Deployment e Considerazioni finali