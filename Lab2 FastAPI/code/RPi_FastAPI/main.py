from fastapi import FastAPI
from fastapi import APIRouter, Request
from fastapi.staticfiles import StaticFiles
from fastapi.exceptions import RequestValidationError
from fastapi.responses import RedirectResponse
from starlette.datastructures import URL

from routes import router

api_router = APIRouter()
api_router.include_router(router, prefix="", tags=["register", "database"])


app = FastAPI()
app.include_router(api_router)
app.mount("/static", StaticFiles(directory="static"), name="static")

@app.exception_handler(RequestValidationError)
def handle_validation_alerts(request: Request, e: RequestValidationError):
    """
    Handles validation errors and turns them into a redirect back with an alert query param.
    This enables us to use proper validation methods, without undue checks in routes.
    """

    url: URL = request.url
    message = "Bad Request: " + ', '.join([m['loc'][-1] + ": " + m['msg'] for m in e.errors()])
    url = url.include_query_params(alert=message)

    # Using 302 to turn the POST into a GET
    return RedirectResponse(url, status_code=302)