#include <WiFi.h>
#include <PubSubClient.h>

#define RELAY_PIN 27


const char* ssid = "*****";
const char* password = "*****";

const char* mqttServer = "*****";   // Broker IP
const int mqttPort = 1883;
const char* commandTopic = "*****";

WiFiClient wifiClient;
PubSubClient mqtt(wifiClient);


void setup() {
    Serial.begin(115200);
    delay(1000);   // Give the Serial Monitor time to connect (optional)

    connectWifi();
    mqtt.setServer(mqttServer, mqttPort);
    mqtt.setCallback(mqttCallback);
    connectMqtt();


    Serial.println();
    Serial.println("ESP32 Relay Test Starting...");

    pinMode(RELAY_PIN, OUTPUT);
    digitalWrite(RELAY_PIN, LOW);

    Serial.println("Relay initialized to OFF");
}





void loop() {
     if (WiFi.status() != WL_CONNECTED) {
        connectWifi();
    }

    if (!mqtt.connected()) {
        connectMqtt();
    }

    // Required for receiving MQTT messages
    mqtt.loop();


}

void activateRelay() {
    Serial.println("Relay ON");
    digitalWrite(RELAY_PIN, HIGH);
    delay(300);

    Serial.println("Relay OFF");
    digitalWrite(RELAY_PIN, LOW);
}

void connectWifi() {

    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }

    Serial.println();
    Serial.println("WiFi Connected");
}

void connectMqtt() {
    while (!mqtt.connected()) {
        Serial.print("Connecting to MQTT...");

        String clientId = "ESP32-Gate-";
        clientId += String((uint32_t)ESP.getEfuseMac(), HEX);

        if (mqtt.connect(clientId.c_str(),"*****","*****")) {
            Serial.println("connected");

            bool subscribed = mqtt.subscribe(commandTopic);

            Serial.print("Subscribe to ");
            Serial.print(commandTopic);
            Serial.print(": ");
            Serial.println(subscribed ? "successful" : "failed");

        } else {
            Serial.print("failed, state=");
            Serial.println(mqtt.state());

            delay(5000);
        }
    }
}

void mqttCallback(char* topic, byte* payload, unsigned int length) {
    Serial.println();
    Serial.println("MQTT message received");

    Serial.print("Topic: ");
    Serial.println(topic);

    Serial.print("Message: ");

    String message;

    for (unsigned int i = 0; i < length; i++) {
        char character = (char) payload[i];

        Serial.print(character);
        message += character;
    }

    Serial.println();

    message.trim();

    if (message == "OPEN") {
        Serial.println("Opening gate...");
        // digitalWrite(RELAY_PIN, HIGH);
        activateRelay();
    } else if (message == "CLOSE") {
        Serial.println("Closing gate...");
        // digitalWrite(RELAY_PIN, LOW);
    } else {
        Serial.println("Unknown command");
    }
}