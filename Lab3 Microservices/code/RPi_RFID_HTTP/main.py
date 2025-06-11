import json
import requests
import time
from models import RfidDataPoint
# import RPi.GPIO as GPIO
# from mfrc522 import SimpleMFRC522
from time import sleep
import random

HOST = "http://ingress-api.daellhin.cloudandmobile.ilabt.imec.be/"
sensor_name = "RPi"

count = 0
RFID_tags = []

def load_startup_data():
    global count, RFID_tags

    try:
        r_tags = requests.get(url=HOST + "current", params={})
        r_tags.raise_for_status()
        r_count = requests.get(url=HOST + "count", params={})
        r_count.raise_for_status()

        RFID_tags = [int(tag) for tag in json.loads(r_tags.text)]
        count = int(r_count.text)
        print(f"Loaded startup data: {count} people in the building with tags {RFID_tags}")
    except requests.exceptions.RequestException as e:
        print(f"Error loading startup data: {e}")

def process_RFID_tag(tag_id: int):
    global count

    if tag_id in RFID_tags:
        count = count - 1
        RFID_tags.remove(tag_id)
        print(f"        {tag_id} left the building")
    else:
        RFID_tags.append(tag_id)
        count = count + 1
        print(f"        {tag_id} entered the building")

    current_milliseconds = int(time.time() * 1000)
    data_point = RfidDataPoint(timestamp=current_milliseconds, rfid_id=str(tag_id), count=count, sensor_name=sensor_name)

    try:
        r = requests.post(url=HOST + "data", params={}, json=data_point.to_dict())
        r.raise_for_status()
        print(f"        Data posted successfully: {r.status_code}")
    except requests.exceptions.RequestException as e:
        print(f"        Error posting data: {e}")

    print(f"        {count} people in the building")

# reader = SimpleMFRC522()
if __name__ == "__main__":
    load_startup_data()
    print("RFID scanner started")
    try:
        while True:
            # -- Actual reading --
            # read_id, text = reader.read()
            # print(f"Detected {read_id}")
            # process_RFID_tag(read_id)
            # sleep(1)
            # -- Simulated reading --
            random_id = random.randint(0, 10)
            print(f"Detected {random_id}")
            process_RFID_tag(random_id)
            sleep(1)
    finally:
        # GPIO.cleanup()
        pass
