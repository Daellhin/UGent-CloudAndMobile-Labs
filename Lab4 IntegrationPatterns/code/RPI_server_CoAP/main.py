from coapthon.server.coap import CoAP
from coapthon.resources.resource import Resource


class BasicResource(Resource):
    def __init__(self, name="BasicResource", coap_server=None):
        super(BasicResource, self).__init__(name, coap_server, visible=True, observable=True, allow_children=True)
        self.payload = "Basic Resource"

    def render_POST(self, request):
        res = BasicResource()
        res.location_query = request.uri_query
        res.payload = request.payload
        print(res.payload)
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
