/*
 Basic ESP8266 MQTT example

 This sketch demonstrates the capabilities of the pubsub library in combination
 with the ESP8266 board/library.

*/

#include <WiFi.h>
#include <PubSubClient.h>

#include <SPI.h>
#include <MFRC522.h>

#define SS_PIN 5 // ESP32 pin GPIO5
#define RST_PIN 27 // ESP32 pin GPIO27

MFRC522 rfid(SS_PIN, RST_PIN);


// Update these with values suitable for your network.

const char* ssid = "UGent_IoT_Lab";
const char* password = "ugent_iot_lab";  
const char* mqtt_server = "192.168.0.46";
const int mqtt_server_port = 1883;
const char* mqtt_username = "";
const char* mqtt_password = "";

WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];
int value = 1234567890;

void setup_wifi() {

  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Create a random client ID
    String clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);
    // Attempt to connect
    // CHANGE LINE BELOW FOR CONNECTING WITH USERNAME AND PASSWORD
    if (client.connect(clientId.c_str())) { //, mqtt_username, mqtt_password)) {
      Serial.println("connected");
      // Once connected, publish an announcement...
      // client.publish("esp32/tagscan", "hello world");
      // ... and resubscribe
      client.subscribe("esp32/tagscan");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, mqtt_server_port); 
  client.setCallback(callback);

  SPI.begin(); // init SPI bus
  rfid.PCD_Init(); // init MFRC522
}

void array_to_string(byte array[], unsigned int len, char buffer[]) {
   for (unsigned int i = 0; i < len; i++) {
      byte nib1 = (array[i] >> 4) & 0x0F;
      byte nib2 = (array[i] >> 0) & 0x0F;
      buffer[i*2+0] = nib1  < 0xA ? '0' + nib1  : 'A' + nib1  - 0xA;
      buffer[i*2+1] = nib2  < 0xA ? '0' + nib2  : 'A' + nib2  - 0xA;
   }
   buffer[len*2] = '\0';
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  if (rfid.PICC_IsNewCardPresent()) { // new tag is available
    if (rfid.PICC_ReadCardSerial()) { // NUID has been readed
      char buffer[rfid.uid.size];
      array_to_string(rfid.uid.uidByte, rfid.uid.size, buffer);
      Serial.printf("Detected Tag: %s\n", buffer);
      
      Serial.printf("Publish message: %s\n", buffer);
      client.publish("esp32/tagscan", buffer);

      rfid.PICC_HaltA(); // halt PICC
      rfid.PCD_StopCrypto1(); // stop encryption on PCD
      delay(500);
    }
  }
  delay(100);
}
