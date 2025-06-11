from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    bucket: str = 'default'
    influx_org: str = 'speybrouck-lorin'
    log_level: str = 'INFO'
    influx_token: str = "1udA9YHaKeiUcT2RIRwUZbu0w7mNCrlZkpnES-9Xpp_nd1eHva22v5Pyb3l72sZqCUF6A6L5z5n6x37UWHkXgg=="
    influx_url: str = "http://influx.database.cloudandmobile.ilabt.imec.be"
