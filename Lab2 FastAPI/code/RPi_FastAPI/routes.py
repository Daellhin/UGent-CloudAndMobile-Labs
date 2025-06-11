from typing import Optional
from fastapi import APIRouter, Request, Depends
from fastapi.templating import Jinja2Templates
from models import Registration, RFIDuser
from MyRPI import MyRPi

rpi = MyRPi()

users = []

templates = Jinja2Templates(directory="templates")
router = APIRouter()

@router.get("/")
async def root(request: Request, alert:Optional[str] = None):
    return templates.TemplateResponse("index.html", {"request": request, "alert": alert})


@router.get("/hello/{name}")
async def say_hello(request: Request, name: str, alert:Optional[str] = None):
    return templates.TemplateResponse("index.html", {"request":request, "name":name, "alert": alert})


@router.get("/register")
def show_registration(request: Request, alert:Optional[str] = None):
    return templates.TemplateResponse("register.html", {"request": request, "alert": alert})

@router.post("/register")
def register(request: Request, data: Registration = Depends(), alert:Optional[str] = None):
    temp = RFIDuser(data.firstname, data.lastname, data.email, data.rfidid)
    if temp in users:
        return templates.TemplateResponse("index.html", {
            "request": request,
            "alert": "User with this RFID already exists"
        })
    users.append(temp)
    print("Registered", data.firstname, data.lastname)
    return templates.TemplateResponse("index.html",{
            "request": request,
            "name": data.firstname,
            "alert": "Succusfully registered!"
    })

@router.get("/control_led")
def show_control_led(request: Request):
    light_status = rpi.get_status()
    return templates.TemplateResponse("control_led.html", {
        "request": request,
        "lightStatus": light_status
    })

@router.get("/users")
def get_users(request: Request):
    return templates.TemplateResponse("users.html", {
        "request": request,
        "users": users
    })

@router.post("/toggle_led/{status}")
def toggle_led(request: Request, status: str):
    print("Turn light", status)
    new_status = True if status == "on" else False
    rpi.set_status(new_status)
    light_status = rpi.get_status()

    return templates.TemplateResponse("control_led.html", {
        "request": request,
        "lightStatus": light_status
    })

# function control_led needs to handle a post operation
