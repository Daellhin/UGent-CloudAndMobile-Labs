from coapthon.server.coap import CoAP
from coapthon.resources.resource import Resource
import json
import requests
from models import RfidDataPoint
import time

HOST = "http://ingress-api.daellhin.cloudandmobile.ilabt.imec.be/"
sensor_name = "ESP32-via-RPI"

count = 0
RFID_tags = []

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

class BasicResource(Resource):
    def __init__(self, name="BasicResource", coap_server=None):
        super(BasicResource, self).__init__(name, coap_server, visible=True, observable=True, allow_children=True)
        self.payload = "Basic Resource"

    def render_POST(self, request):
        res = BasicResource()
        res.location_query = request.uri_query
        res.payload = request.payload
        print(res.payload)
        process_RFID_tag(res.payload)
        return res


class CoAPServer(CoAP):
    def __init__(self, host, port):
        CoAP.__init__(self, (host, port))
        self.add_resource('sensor/', BasicResource())


def main():
    server = CoAPServer("0.0.0.0", 5683)
    print("Server started")
    try:
        server.listen(10)
    except KeyboardInterrupt:
        print("Server Shutdown")
        server.close()
        print("Exiting...")


if __name__ == '__main__':
    main()
