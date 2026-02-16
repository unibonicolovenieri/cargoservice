import paho.mqtt.client as paho
import time

# --- CONFIGURAZIONE ---
brokerAddr = "10.249.112.38"  # L'IP del tuo PC dove gira Docker
topic = "unibo/sonar/events"

# Funzione chiamata quando il client si connette al broker
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print(f"Connesso al broker! Errore: {rc}")
        # Una volta connessi, ci iscriviamo al topic
        client.subscribe(topic)
        print(f"Iscritto al topic: {topic}")
    else:
        print(f"Connessione fallita, codice: {rc}")

# Funzione chiamata quando arriva un messaggio
def on_message(client, userdata, msg):
    print(f"Messaggio ricevuto sul topic {msg.topic}:")
    print(f" -> {msg.payload.decode()}")

# --- MAIN ---
if __name__ == '__main__':
    # Creazione client (usando la stessa versione API del tuo script precedente)
    client = paho.Client(paho.CallbackAPIVersion.VERSION1, "listener_client")
    
    # Assegnazione delle funzioni di callback
    client.on_connect = on_connect
    client.on_message = on_message

    print(f"In connessione al broker {brokerAddr}...")
    
    try:
        client.connect(brokerAddr, 1883, 60)
        
        # loop_forever() blocca il programma e resta in ascolto continuo.
        # È perfetto per un subscriber dedicato.
        client.loop_forever()
        
    except KeyboardInterrupt:
        print("\nChiusura listener...")
        client.disconnect()
