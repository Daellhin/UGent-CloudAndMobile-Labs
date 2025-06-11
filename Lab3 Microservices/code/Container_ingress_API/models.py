from typing import List, Self
from pydantic import BaseModel


class RfidDataPoint(BaseModel):
    timestamp: int
    rfid_id: str
    count: int
    sensor_name: str


class TagEntry(BaseModel):
    timestamp: int
    rfid_id: str
    sensor_name: str


class CountEntry(BaseModel):
    timestamp: int
    count: int
    sensor_name: str


class RecentData(BaseModel):
    rfid_ids: List[TagEntry]
    counts: List[CountEntry]

    def merge(self, other: Self) -> Self:
        self.rfid_ids.extend(other.rfid_ids)
        self.counts.extend(other.counts)

        return self

    @classmethod
    def from_records(cls, records) -> Self:
        raise NotImplementedError
