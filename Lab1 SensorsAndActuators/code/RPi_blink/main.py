import RPi.GPIO as GPIO # Import the library
from time import sleep

LED_PIN = 4

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM) # Use BCM pin numbering (GPIOx)
# Initiate pin as output with initial value of LOW (= off)
GPIO.setup(LED_PIN, GPIO.OUT, initial=GPIO.LOW)

try:
    while True:
        GPIO.output(LED_PIN, GPIO.HIGH) # Turn LED on HIGH
        print('Changed LED state to ON')
        sleep(1) # Sleep for 1 second
        GPIO.output(LED_PIN, GPIO.LOW)
        print('Changed LED state to OFF')
        sleep(1)
except KeyboardInterrupt: # Listen to the event triggered by Ctrl-c
    GPIO.cleanup() # Cleanly close the GPIO pins before exiting
    print('Bye bye!')