#!/usr/bin/env python3
from math import floor
import time
import random
import requests

def main():
    inside = []
    count = 0
    while True:
        timestamp_ms = floor(time.time() * 1000)

        tag = ""
        if len(inside) < 1 or random.randint(0, 1) == 0:
            # Invent a new tag and add to inside
            tag = '%030x' % random.randrange(16**10) # random 10-char hex string
            inside.append(tag)
            count = count + 1
        else:
            tag = random.choice(inside)
            inside.remove(tag)
            count = count - 1

        body = {
            "timestamp": timestamp_ms,
            "rfid_id": tag,
            "count": count,
            "sensor_name": "test_script"
        }

        r = requests.post("http://ingress-api.stpletin.cloudandmobile.ilabt.imec.be/data/", json=body)
        print(r.status_code)
        if r.status_code != 201:
            print(r.text)

        time.sleep(1) # one second

if __name__ == "__main__":
    main()
