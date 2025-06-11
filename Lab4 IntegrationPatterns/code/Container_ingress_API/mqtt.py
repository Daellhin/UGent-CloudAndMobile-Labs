from typing import Any

from gmqtt import Client as MQTTClient
from gmqtt.mqtt.constants import MQTTv311

from fastapi_mqtt import FastMQTT, MQTTConfig

mqtt_config = MQTTConfig(
    host="rabbitmq.database.ilabt.cloudandmobile.ilabt.imec.be",     # change this to the url of the RabbitMQ service
    port=32712,
    keepalive=60,
    username="vhspeybrouck-lorin:speybrouck-lorin",  # change this to your username
    password="GrqrvL6I",  # change this to your password
    version=MQTTv311,
)

fast_mqtt = FastMQTT(config=mqtt_config)

# write your methods here
