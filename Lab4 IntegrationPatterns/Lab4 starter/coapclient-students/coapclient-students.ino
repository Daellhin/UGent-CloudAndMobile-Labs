#include <SPI.h>
#include <Dhcp.h>
#include <Dns.h>
#include <coap-simple.h>
#include <WiFi.h>
#include <WiFiUdp.h>

#include <SPI.h>
#include <MFRC522.h>

#define SS_PIN 5 // ESP32 pin GPIO5
#define RST_PIN 27 // ESP32 pin GPIO27

MFRC522 rfid(SS_PIN, RST_PIN);

// Replace the next variables with your WiFi SSID (network name)/Password combination
const char* ssid = "UGent_IoT_Lab"; //"UGent_IoT_Lab";
const char* password = "ugent_iot_lab";

// CoAP client response callback
void callback_response(CoapPacket &packet, IPAddress ip, int port);

// UDP and CoAP class
WiFiUDP Udp;
Coap coap(Udp);

// CoAP client response callback
void callback_response(CoapPacket &packet, IPAddress ip, int port) {
  Serial.println("[Coap Response obtained]");

  char p[packet.payloadlen + 1];
  memcpy(p, packet.payload, packet.payloadlen);
  p[packet.payloadlen] = NULL;

  Serial.println(p);
}

void setup() {
  Serial.begin(9600);
  setup_wifi();

  // client response callback.
  // this endpoint is the single callback.
  Serial.println("Setup Response Callback");
  coap.response(callback_response);

  // start coap server/client
  coap.start();
}

void setup_wifi() {
  delay(100);
  // We start by connecting to a WiFi network

  WiFi.begin(ssid, password);

  int counter = 0;
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print("Connecting to ");
    Serial.println(ssid);
    Serial.println("\' ...");
    delay(2000);
    counter += 1;
    if (counter==5) {
      Serial.println("Restarting .. ");
      ESP.restart();
    } 
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  SPI.begin(); // init SPI bus
  rfid.PCD_Init(); // init MFRC522
}

void array_to_string(byte array[], unsigned int len, char buffer[])
{
   for (unsigned int i = 0; i < len; i++)
   {
      byte nib1 = (array[i] >> 4) & 0x0F;
      byte nib2 = (array[i] >> 0) & 0x0F;
      buffer[i*2+0] = nib1  < 0xA ? '0' + nib1  : 'A' + nib1  - 0xA;
      buffer[i*2+1] = nib2  < 0xA ? '0' + nib2  : 'A' + nib2  - 0xA;
   }
   buffer[len*2] = '\0';
}

void loop() {
  if (rfid.PICC_IsNewCardPresent()) { // new tag is available
    if (rfid.PICC_ReadCardSerial()) { // NUID has been readed
      MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak);
      Serial.print("RFID/NFC Tag Type: ");
      Serial.println(rfid.PICC_GetTypeName(piccType));
      // print UID in Serial Monitor in the hex format
      Serial.print("UID:");

      for (int i = 0; i < rfid.uid.size; i++) {
        Serial.print(rfid.uid.uidByte[i] < 0x10 ? " 0" : " ");
        Serial.print(rfid.uid.uidByte[i], HEX);
      }
      Serial.println();

      char buffer[rfid.uid.size];
      array_to_string(rfid.uid.uidByte, rfid.uid.size, buffer);
      Serial.println(buffer);

      Serial.print("Sending ");
      Serial.println(buffer);
      uint8_t* val = reinterpret_cast<uint8_t *>(buffer);
      //send to correct IP address (of RPi), port, resource name, type of connection (here confirmable), HTTP method, 
      //token, token length, then finally your payload or message, and the payload size in bytes
      //see https://github.com/hirotakaster/CoAP-simple-library/blob/master/coap-simple.h for more information
      int msgid =  coap.send(IPAddress(192, 168, 0, 46), 5683, "sensor", COAP_CON, COAP_POST, NULL, 0, val, 8);

      Serial.println();
      rfid.PICC_HaltA(); // halt PICC
      rfid.PCD_StopCrypto1(); // stop encryption on PCD
      delay(500);
    }
  }
  
  coap.loop();
}
