from dataclasses import dataclass, asdict


@dataclass
class RfidDataPoint:
    timestamp: int
    rfid_id: str
    count: int
    sensor_name: str

    def to_dict(self) -> dict:
        return asdict(self)