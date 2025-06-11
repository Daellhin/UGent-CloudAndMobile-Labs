# import RPi.GPIO as GPIO
# TODO: uncomment line above if you are running on the RPi!

LED_PIN = 4


class RaspberryPi(type):
    """
    Implementation of Singleton pattern
    """
    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(RaspberryPi, cls).__call__(*args, **kwargs)
        return cls._instances[cls]


class MyRPi(metaclass=RaspberryPi):
    """
    Class that is responsible for accessing and setting the pins on the RPi
    """
    def __init__(self):
        """
        Initiate the LED_PIN as OUTPUT and initial value of LOW
        """
        # GPIO.setmode(GPIO.BCM)
        # GPIO.setup(LED_PIN, GPIO.OUT, initial=GPIO.LOW)

    def get_status(self):
        """
        Method to retrieve the status of the actuator
        :return: status as boolean (HIGH / LOW)
        """
        # return GPIO.input(LED_PIN) == GPIO.HIGH
        return 0

    def set_status(self, status):
        """
        Method to set the status of the actuator
        :param status: boolean (HIGH / LOW)
        """
        # if status:
        #     GPIO.output(LED_PIN, GPIO.HIGH)
        # else:
        #     GPIO.output(LED_PIN, GPIO.LOW)
