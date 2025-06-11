from contextlib import asynccontextmanager
from typing import AsyncGenerator
from mqtt import fast_mqtt

@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncGenerator[None, None]:
    print("Lifespan method called.")
    await fast_mqtt.connection()
    yield
    await fast_mqtt.client.disconnect()

# change the app creation line to:
app = FastAPI(lifespan=lifespan)


