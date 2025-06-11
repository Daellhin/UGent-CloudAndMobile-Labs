import logging

from fastapi import FastAPI, HTTPException
from functools import reduce
from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
from influxdb_client.domain.write_precision import WritePrecision
from typing import List, Any, Coroutine
from datetime import datetime, timedelta
import time
from config import Settings
from models import CountEntry, RfidDataPoint, RecentData, TagEntry

from contextlib import asynccontextmanager
from typing import AsyncGenerator
from mqtt import fast_mqtt
from gmqtt import Client as MQTTClient

@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncGenerator[None, None]:
    print("Lifespan method called.")
    await fast_mqtt.connection()
    yield
    await fast_mqtt.client.disconnect()

# change the app creation line to:
app = FastAPI(lifespan=lifespan)
settings = Settings()
influx = InfluxDBClient(url=settings.influx_url, token=settings.influx_token, org=settings.influx_org)
logging.basicConfig()
logger = logging.getLogger(__name__)
logger.setLevel(settings.log_level.upper())
logger.info(f"Logger initialized")

@app.get("/", description="Example route, also used for healthchecks")
async def root():
    try:
        delete_api = influx.delete_api()

        start = "1970-01-01T00:00:00Z"
        stop = "2099-12-31T23:59:59Z"
        delete_api.delete(start, stop, predicate='', bucket=settings.bucket, org=settings.influx_org)

        print(f"All data deleted from bucket: {settings.bucket}")
        return {"message": "Data deleted"}
    except Exception as e:
        logger.error(f"Error deleting all data: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
    # return {"message": "Hello World"}


@app.post("/data/", status_code=201, description="Store sensor data in InfluxDB")
async def post_data(data: RfidDataPoint):
    try:
        write_api   = influx.write_api(write_options=SYNCHRONOUS)

        people_point = Point("people").tag("source", data.sensor_name).field("value", data.count).time(data.timestamp, WritePrecision.MS)
        rfid_point = Point("raw_ids").tag("source", data.sensor_name).field("value", data.rfid_id).time(data.timestamp, WritePrecision.MS)

        # Write the data point to InfluxDB
        write_api.write(
            bucket=settings.bucket,
            org=settings.influx_org,
            record=[people_point, rfid_point]
        )

        logger.info(f"Data stored: {data}")
        return {"status": "success"}
    except Exception as e:
        logger.error(f"Error storing sensor data: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/data/", description="Retrieve recent data")
async def get_data(since: str = "1h") -> RecentData:
    try:
        query_api = influx.query_api()
        
        count_query = f'''
            from(bucket: "{settings.bucket}")
            |> range(start: -{since})
            |> filter(fn: (r) => r["_measurement"] == "people")
            |> filter(fn: (r) => r["_field"] == "value")
        '''

        rfid_query = f'''
            from(bucket: "{settings.bucket}")
            |> range(start: -{since})
            |> filter(fn: (r) => r["_measurement"] == "raw_ids")
            |> filter(fn: (r) => r["_field"] == "value")
        '''
        
        count_tables = query_api.query(count_query, org=settings.influx_org)
        rfid_tables = query_api.query(rfid_query, org=settings.influx_org)
        
        # Process count results
        counts = [
            CountEntry(timestamp=int(record.get_time().timestamp()), sensor_name=record.values.get("source"), count=record.get_value())
            for table in count_tables for record in table.records
        ]
        
        # Process RFID results
        rfids = [
            TagEntry(timestamp=int(record.get_time().timestamp()), sensor_name=record.values.get("source"), rfid_id=record.get_value())
            for table in rfid_tables for record in table.records
        ]
        
        logger.info(f"Retrieved {len(counts)} count records and {len(rfids)} RFID records since {since}")
        return RecentData(counts=counts, rfid_ids=rfids)
    except Exception as e:
        logger.error(f"Error retrieving recent data: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/count/", description="Retrieve last count")
async def get_count() -> int:
    try:
        query_api = influx.query_api()
        
        # Query for the most recent count
        count_query = f'''
            from(bucket: "{settings.bucket}")
            |> range(start: today())
            |> filter(fn: (r) => r["_measurement"] == "people")
            |> filter(fn: (r) => r["_field"] == "value")
            |> sort(columns: ["_time"], desc: true)
            |> limit(n: 1)
        '''
        
        result = query_api.query(count_query, org=settings.influx_org)
        
        # Check if there are any records
        if not result or not result[0].records:
            return 0
        
        # Get the value from the first (and only) record
        latest_count = result[0].records[0].get_value()
        
        logger.info(f"Retrieved latest count: {latest_count}")
        return int(latest_count)
    except Exception as e:
        logger.error(f"Error retrieving latest count: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/current/", description="Get tags currently inside")
async def get_current() -> List[str]:
    try:
        query_api = influx.query_api()
        current_query = f'''
                from(bucket: "{settings.bucket}")
                  |> range(start: today())
                  |> filter(fn: (r) => r._measurement == "raw_ids" and r._field == "value")
                  |> duplicate(column: "_value", as: "rfid_id")
                  |> group(columns: ["rfid_id"])
                  |> count()
                  |> filter(fn: (r) => r._value % 2 != 0)
                  |> rename(columns: {{"_value": "count"}})
                  |> keep(columns: ["rfid_id", "count"])
        '''
        current_tables = query_api.query(current_query)

        tags = [ record.values["rfid_id"] for table in current_tables for record in table.records]
        logger.info(f"Retrieved current tags: {tags}")
        return tags
    except Exception as e:
        logger.error(f"Error retrieving latest count: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@fast_mqtt.on_connect()
def connect(client: MQTTClient, flags: int, rc: int, properties: Any):
    print(f"Connected to {client._host}")

@fast_mqtt.on_disconnect()
def disconnect(client: MQTTClient, packet, exc=None):
    print(f"Disconnected from {client._host}")

@fast_mqtt.on_subscribe()
def subscribe(client: MQTTClient, mid: int, qos: int, properties: Any):
    print("subscribed: ", mid, qos, properties)

@fast_mqtt.subscribe("esp32/tagscan", qos=1)
async def tagscan(client: MQTTClient, topic: str, payload: bytes, qos: int, properties: Any):
    print("Tagsscan: ", topic, payload.decode(), qos)

    current_milliseconds = int(time.time() * 1000)
    await post_data(RfidDataPoint(timestamp=current_milliseconds, rfid_id=payload.decode(), count=1, sensor_name="esp32"))
