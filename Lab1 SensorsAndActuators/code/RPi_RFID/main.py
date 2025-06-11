import RPi.GPIO as GPIO
from mfrc522 import SimpleMFRC522

while True:
    reader = SimpleMFRC522()
    try:
        tag_id, tag_text = reader.read()
        print(tag_id)
        print(tag_text)
    finally:
        GPIO.cleanup()