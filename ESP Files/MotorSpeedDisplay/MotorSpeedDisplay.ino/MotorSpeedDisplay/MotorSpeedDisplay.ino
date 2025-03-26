#include <WiFi.h>    // Use WiFi.h for ESP32, ESP8266WiFi.h for ESP8266
#include <PubSubClient.h>   // MQTT library

const char* ssid = "OnePlus 12R";
const char* password = "m3ntui43";

// MQTT Broker details
const char* hivemq_broker = "broker.hivemq.com";
const char* mosquitto_broker = "test.mosquitto.org";
const int mqtt_port = 1883;

// Topic
const char* mqtt_topic = "bms/gp_motor";

// Variables to track broker switching
bool useHiveMQ = true;  // Start with HiveMQ

WiFiClient espClient;
PubSubClient client(espClient);

void setup() {
    Serial.begin(9600);

    WiFi.begin(ssid, password);
    Serial.print("Connecting to WiFi...");
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("\nConnected to WiFi");

    // Start with HiveMQ, fallback to Mosquitto if needed
    switchBroker();
}

void switchBroker() {
    if (useHiveMQ) {
        client.setServer(hivemq_broker, mqtt_port);
        Serial.println("🟢 Trying to connect to HiveMQ...");
    } else {
        client.setServer(mosquitto_broker, mqtt_port);
        Serial.println("🔵 Trying to connect to Mosquitto...");
    }
}

void reconnect() {
    while (!client.connected()) {
        Serial.print("Connecting to MQTT...");

        // Attempt connection
        if (client.connect("ESP_Client")) {
            Serial.println("✅ Connected to MQTT broker!");
            client.subscribe(mqtt_topic);
            return;
        } else {
            Serial.print("❌ Failed, rc=");
            Serial.print(client.state());
            Serial.println(". Switching broker...");

            // Switch to the other broker
            useHiveMQ = !useHiveMQ;
            switchBroker();

            // Wait before retrying
            delay(5000);
        }
    }
}

float measureSpeed() {
    float speed = random(50, 150); // Random speed between 50-150 RPM
    return speed;
}

void loop() {
    if (!client.connected()) {
        reconnect();
    }
    client.loop();

    float speed = measureSpeed();
    Serial.print("Motor Speed: ");
    Serial.print(speed);
    Serial.println(" RPM");

    char msg[20];
    dtostrf(speed, 6, 2, msg);
    client.publish(mqtt_topic, msg);

    delay(500);
}
