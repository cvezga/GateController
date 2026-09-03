#include <WiFi.h>
#include <PubSubClient.h>

#define RELAY_PIN 27
#define RELAY_ON LOW
#define RELAY_OFF HIGH


const char* ssid = "*****";
const char* password = "*****";

const char* mqttServer = "*****";   // Broker IP
const int mqttPort = 1883;
const char* commandTopic = "garage/gate/command";
const char* confirmationTopic = "garage/gate/confiormation";
const char* user = "user";
const char* password = "password";

WiFiClient wifiClient;
PubSubClient mqtt(wifiClient);


void setup() {
    Serial.begin(115200);
    delay(1000);   // Give the Serial Monitor time to connect (optional)

    connectWifi();
    mqtt.setServer(mqttServer, mqttPort);
    mqtt.setCallback(mqttCallback);
    mqtt.setBufferSize(512);

    connectMqtt();

    Serial.println("ESP32 Gate Controller Started");

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

        if (mqtt.connect(clientId.c_str(),user,password)) {
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

    String confirmationMessage;

    if (message.startsWith("OPEN-")) {
        Serial.println("Opening gate...");
        // digitalWrite(RELAY_PIN, HIGH);
        activateRelay();
        confirmationMessage = message + "-OK";
    } else if (message.startsWith("OPEN-")) {
        Serial.println("Closing gate...");
        // digitalWrite(RELAY_PIN, LOW);
        confirmationMessage = message + "-Not suported";
    } else {
        Serial.println("Unknown command");
        confirmationMessage = message + "-Unknown command";
    }

    // Send confirmation
    if (mqtt.connected()) {

        bool published = mqtt.publish(
            confirmationTopic,
            confirmationMessage.c_str()
        );

        Serial.print("Confirmation: ");
        Serial.println(confirmationMessage);

        Serial.print("Publish: ");
        Serial.println(published ? "successful" : "failed");
    }
}