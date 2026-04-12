from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from app.config.settings import get_settings
from app.exceptions import AppError
from app.routers import auth, clients, staffs, invitations, gate, notifications

settings = get_settings()

app = FastAPI(title="authorization-python")

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.frontend_url],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    allow_headers=["Content-Type", "Authorization"],
)


@app.exception_handler(AppError)
async def app_error_handler(request: Request, exc: AppError) -> JSONResponse:
    return JSONResponse(status_code=exc.status_code, content={"message": exc.message})


# ルーター登録
prefix = "/api"
app.include_router(auth.router, prefix=prefix)
app.include_router(clients.router, prefix=prefix)
app.include_router(staffs.router, prefix=prefix)
app.include_router(invitations.router, prefix=prefix)
app.include_router(gate.router, prefix=prefix)
app.include_router(notifications.router, prefix=prefix)
