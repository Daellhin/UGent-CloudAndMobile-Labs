from typing import Any

from gmqtt import Client as MQTTClient
from gmqtt.mqtt.constants import MQTTv311

from fastapi_mqtt import FastMQTT, MQTTConfig

mqtt_config = MQTTConfig(
    host="",     # change this to the url of the RabbitMQ service
    port=32712,
    keepalive=60,
    username="",  # change this to your username
    password="",  # change this to your password
    version=MQTTv311,
)

fast_mqtt = FastMQTT(config=mqtt_config)

# write your methods here
