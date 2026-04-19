import httpx
from fastapi import APIRouter, Depends, Response, Request
from fastapi.responses import RedirectResponse
from app.config.settings import Settings, get_settings
from app.exceptions import unauthorized, bad_request
from app.routers.deps import (
    get_auth_interactor, get_invitation_interactor, get_staff_id_from_cookie,
)
from app.usecase.auth.interactor import AuthInteractor
from app.usecase.auth.dto import AuthLoginDto
from app.usecase.invitation.interactor import InvitationInteractor

router = APIRouter()

# OAuth はブラウザリダイレクトのため /api 外に配置（PHP と同じパス構造）
oauth_router = APIRouter()

GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"
GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo"


@router.get("/auth/me")
def get_my_profile(
    staff_id: int = Depends(get_staff_id_from_cookie),
    interactor: AuthInteractor = Depends(get_auth_interactor),
):
    if staff_id == 0:
        raise unauthorized("unauthenticated")
    staff = interactor.find_user(staff_id)
    if staff is None:
        raise unauthorized("unauthenticated")
    return {
        "staff_id": staff.id,
        "name": staff.name,
        "avatar": staff.avatar,
        "role": staff.role,
    }


@router.get("/auth/login")
def login(settings: Settings = Depends(get_settings)):
    return {"login_url": f"{settings.frontend_url}/login"}


@router.get("/auth/logout")
def logout(response: Response):
    response.delete_cookie("staff_id")
    return {"message": "logged_out"}


@router.get("/auth/invitation/{token}")
def invitation(
    token: str,
    invitation_interactor: InvitationInteractor = Depends(get_invitation_interactor),
):
    inv = invitation_interactor.find_by_token(token)
    return {"token": inv.token}


@oauth_router.get("/auth/google/redirect")
def google_redirect(settings: Settings = Depends(get_settings)):
    params = {
        "client_id": settings.google_client_id,
        "redirect_uri": settings.google_redirect_url,
        "response_type": "code",
        "scope": "openid email profile",
        "access_type": "offline",
    }
    query = "&".join(f"{k}={v}" for k, v in params.items())
    return RedirectResponse(url=f"{GOOGLE_AUTH_URL}?{query}", status_code=302)


@oauth_router.get("/auth/google/callback")
def google_callback(
    code: str = "",
    settings: Settings = Depends(get_settings),
    interactor: AuthInteractor = Depends(get_auth_interactor),
):
    if not code:
        raise bad_request("code_required")

    with httpx.Client() as client:
        token_resp = client.post(GOOGLE_TOKEN_URL, data={
            "code": code,
            "client_id": settings.google_client_id,
            "client_secret": settings.google_client_secret,
            "redirect_uri": settings.google_redirect_url,
            "grant_type": "authorization_code",
        })
        token_data = token_resp.json()
        access_token = token_data.get("access_token")
        if not access_token:
            raise unauthorized("token_exchange_failed")

        user_resp = client.get(GOOGLE_USERINFO_URL, headers={"Authorization": f"Bearer {access_token}"})
        user_info = user_resp.json()

    dto = AuthLoginDto(
        provider=1,  # Provider::Google
        provider_id=user_info["id"],
        name=user_info.get("name", ""),
        email=user_info.get("email", ""),
        avatar=user_info.get("picture"),
    )
    staff = interactor.login(dto)

    max_age = settings.staff_cookie_lifetime * 60
    redirect = RedirectResponse(url=f"{settings.frontend_url}/clients", status_code=302)
    redirect.set_cookie("staff_id", str(staff.id), max_age=max_age, httponly=True, samesite="lax")
    return redirect
