import RPi.GPIO as GPIO
import time
import sys
import paho.mqtt.client as paho

### CONFIGURATION
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

LED_PIN = 25
TRIG = 17
ECHO = 27

GPIO.setup(LED_PIN, GPIO.OUT)
GPIO.setup(TRIG, GPIO.OUT)
GPIO.setup(ECHO, GPIO.IN)

### MQTT
brokerAddr = "10.249.112.148"
msg_template = "msg(sonardata,event,sonar,none,distance(D),N)"
n = 1
client = paho.Client(paho.CallbackAPIVersion.VERSION1, "sonarAndLed")

### DEFINIZIONE FUNZIONI (Sempre prima dell'esecuzione)

def ledOn():
    GPIO.output(LED_PIN, GPIO.HIGH)

def ledOff():
    GPIO.output(LED_PIN, GPIO.LOW)

def forward(distance):
    global n
    n = n + 1
    payload = msg_template.replace("D", str(round(distance, 2))).replace("N", str(n))
    client.publish("unibo/sonar/events", payload)

def applLogic(distance):
    if 0.0 < distance < 5.0:
        ledOn()
        forward(distance)
    else:
        ledOff()

def sonarWork():
    GPIO.output(TRIG, True)
    time.sleep(0.00001)
    GPIO.output(TRIG, False)

    pulse_start = time.time()
    pulse_end = time.time()

    # Attendi che ECHO parta
    while GPIO.input(ECHO) == 0:
        pulse_start = time.time()

    # Attendi che ECHO finisca
    while GPIO.input(ECHO) == 1:
        pulse_end = time.time()

    pulse_duration = pulse_end - pulse_start
    dist = pulse_duration * 17165
    return dist

def init():
    GPIO.output(TRIG, False)
    print(f"Connecting to broker: {brokerAddr}...")
    client.connect(brokerAddr, 1883, 60)
    print('Waiting a few seconds for the sensor to settle')
    time.sleep(2)

def doJob():
    init()
    while True:
        d = sonarWork()
        if 0.0 < d < 150.0:
            print(f"Distance: {d:.2f} cm")
            applLogic(d)
        sys.stdout.flush()
        time.sleep(0.25)

### BLOCCO DI ESECUZIONE (Sempre in fondo!)
if __name__ == '__main__':
    print('sonarAndLed is starting ... ')
    try:
        doJob()
    except KeyboardInterrupt:
        print('\nsonarAndLed BYE ... ')
        GPIO.cleanup()
