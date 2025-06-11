#include "BluetoothSerial.h"

/* Check if Bluetooth configurations are enabled in the SDK */
/* If not, then you have to recompile the SDK */
#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

const int ledPin = 2;

void setup() {
  pinMode(ledPin, OUTPUT);
  Serial.begin(115200);
  /* If no name is given, default 'ESP32' is applied */
  /* If you want to give your own name to ESP32 Bluetooth device, then */
  /* specify the name as an argument SerialBT.begin("myESP32Bluetooth"); */
  SerialBT.begin("ESP Lorin");
  Serial.println("Bluetooth Started! Ready to pair...");
  digitalWrite(ledPin, HIGH);
}

void loop() {
  if (SerialBT.available()) {
    int code = SerialBT.read();
    Serial.println(code);

    if (code == (int)'0') {
      digitalWrite(ledPin, LOW);
    } else if (code == (int)'1') {
      digitalWrite(ledPin, HIGH);
    }
  }
  delay(20);
}