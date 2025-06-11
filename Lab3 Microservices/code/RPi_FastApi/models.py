from typing import Annotated
from dataclasses import dataclass
from fastapi import Form


class RFIDuser:
    def __init__(self, firstname, lastname, email, rfidid):
        self.firstname = firstname
        self.lastname = lastname
        self.rfidid = rfidid
        self.email = email

    def __eq__(self, other):
        if isinstance(other, RFIDuser):
            return (
                    self.firstname == other.firstname and
                    self.lastname == other.lastname and
                    self.email == other.email
            )
        return False

    def __str__(self) -> str:
        return "Name: " + self.firstname  + " " + self.lastname + ", email: " +  self.email + ", RFID ID: " + self.rfidid

@dataclass
class Registration:
    firstname: Annotated[str, Form()]
    lastname: Annotated[str, Form()]
    email: Annotated[str, Form()]
    rfidid: Annotated[str, Form()]
