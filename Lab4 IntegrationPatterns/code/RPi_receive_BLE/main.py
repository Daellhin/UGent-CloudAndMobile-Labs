from bluepy.btle import Peripheral, DefaultDelegate
import struct

class MyDelegate(DefaultDelegate):
    def __init__(self):
        DefaultDelegate.__init__(self)

    def handleNotification(self, cHandle, data):
        for i in range(0, len(data), 4):
            value = struct.unpack('I', data[i:i + 4])[0]
            print(value)

def main():
    ble_address = "24:6F:28:22:51:96"

    print("Connecting to BLE device...")
    peripheral = Peripheral(ble_address)
    print("Connected")

    # Set the delegate to handle notifications
    peripheral.setDelegate(MyDelegate())

    # Assuming the characteristic handle is known and is 0x0012
    # You may need to discover the services and characteristics
    characteristic_handle = 0x0012

    # Enable notifications
    peripheral.writeCharacteristic(characteristic_handle + 1, b"\x01\x00")

    print("Listening for notifications...")

    try:
        while True:
            if peripheral.waitForNotifications(1.0):
                # HandleNotification() was called
                continue
            print("Waiting...")
    except KeyboardInterrupt:
        print("Program terminated")
    finally:
        peripheral.disconnect()

if __name__ == "__main__":
    main()