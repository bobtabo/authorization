"""
認証ルーターモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
import secrets
from collections.abc import Callable

import httpx
from fastapi import APIRouter, Depends, Response, Request
from fastapi.responses import JSONResponse, RedirectResponse
from app.config.settings import Settings, get_settings
from app.exceptions import AppError, unauthorized, bad_request, forbidden
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

GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize"
GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token"
GITHUB_USER_URL = "https://api.github.com/user"
GITHUB_EMAIL_URL = "https://api.github.com/user/emails"

# OAuth 認可開始時に発行する nonce を保持するクッキー名と有効期間（秒）
OAUTH_STATE_COOKIE = "oauth_state"
OAUTH_STATE_COOKIE_MAX_AGE = 600


def _build_oauth_state(settings: Settings, nonce: str, token: str) -> str:
    """state（"{runtime}|{nonce}" または "{runtime}|{nonce}|{token}"）を組み立てる。"""
    return f"{settings.app_runtime}|{nonce}|{token}" if token else f"{settings.app_runtime}|{nonce}"


def _set_oauth_state_cookie(response: Response, settings: Settings, nonce: str) -> None:
    response.set_cookie(
        OAUTH_STATE_COOKIE, nonce, max_age=OAUTH_STATE_COOKIE_MAX_AGE,
        httponly=True, samesite="lax", secure=settings.app_env == "production",
    )


def _consume_oauth_state(request: Request, state: str) -> tuple[str | None, bool]:
    """state の nonce をクッキーと照合し、(招待トークン, 照合結果) を返す。"""
    saved = request.cookies.get(OAUTH_STATE_COOKIE, "")
    parts = state.split("|", 2)
    nonce = parts[1] if len(parts) >= 2 else ""
    if not saved or not nonce or not secrets.compare_digest(saved.encode(), nonce.encode()):
        return None, False
    invitation_token = parts[2] if len(parts) == 3 and parts[2] else None
    return invitation_token, True


def _clear_oauth_state(response: Response) -> None:
    response.delete_cookie(OAUTH_STATE_COOKIE)


def _with_oauth_state_cleared(flow: Callable[[], Response]) -> Response:
    """コールバックの成否を問わず oauth_state クッキーを破棄する。"""
    try:
        response = flow()
    except AppError as e:
        response = JSONResponse(status_code=e.status_code, content={"message": e.message})
    _clear_oauth_state(response)
    return response


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
def google_redirect(token: str = "", settings: Settings = Depends(get_settings)):
    nonce = secrets.token_hex(16)
    params = {
        "client_id": settings.google_client_id,
        "redirect_uri": settings.google_redirect_url,
        "response_type": "code",
        "scope": "openid email profile",
        "access_type": "offline",
        "state": _build_oauth_state(settings, nonce, token),
    }
    query = "&".join(f"{k}={v}" for k, v in params.items())
    redirect = RedirectResponse(url=f"{GOOGLE_AUTH_URL}?{query}", status_code=302)
    _set_oauth_state_cookie(redirect, settings, nonce)
    return redirect


@oauth_router.get("/auth/google/callback")
def google_callback(
    request: Request,
    code: str = "",
    state: str = "",
    settings: Settings = Depends(get_settings),
    interactor: AuthInteractor = Depends(get_auth_interactor),
):
    return _with_oauth_state_cleared(lambda: _google_callback(request, code, state, settings, interactor))


def _google_callback(
    request: Request,
    code: str,
    state: str,
    settings: Settings,
    interactor: AuthInteractor,
) -> Response:
    invitation_token, valid = _consume_oauth_state(request, state)
    if not valid:
        return RedirectResponse(url=f"{settings.frontend_url}/error?code=400", status_code=302)
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
        invitation_token=invitation_token,
    )
    try:
        staff = interactor.login(dto)
    except Exception as e:
        if hasattr(e, "status_code") and e.status_code == 403:
            return RedirectResponse(url=f"{settings.frontend_url}/error?code=403", status_code=302)
        raise

    max_age = settings.staff_cookie_lifetime * 60
    redirect = RedirectResponse(url=f"{settings.frontend_url}/clients", status_code=302)
    redirect.set_cookie("staff_id", str(staff.id), max_age=max_age, httponly=True, samesite="lax")
    return redirect


@oauth_router.get("/auth/github/redirect")
def github_redirect(token: str = "", settings: Settings = Depends(get_settings)):
    nonce = secrets.token_hex(16)
    params = {
        "client_id": settings.github_client_id,
        "redirect_uri": settings.github_redirect_url,
        "scope": "user:email",
        "state": _build_oauth_state(settings, nonce, token),
    }
    query = "&".join(f"{k}={v}" for k, v in params.items())
    redirect = RedirectResponse(url=f"{GITHUB_AUTH_URL}?{query}", status_code=302)
    _set_oauth_state_cookie(redirect, settings, nonce)
    return redirect


@oauth_router.get("/auth/github/callback")
def github_callback(
    request: Request,
    code: str = "",
    state: str = "",
    settings: Settings = Depends(get_settings),
    interactor: AuthInteractor = Depends(get_auth_interactor),
):
    return _with_oauth_state_cleared(lambda: _github_callback(request, code, state, settings, interactor))


def _github_callback(
    request: Request,
    code: str,
    state: str,
    settings: Settings,
    interactor: AuthInteractor,
) -> Response:
    invitation_token, valid = _consume_oauth_state(request, state)
    if not valid:
        return RedirectResponse(url=f"{settings.frontend_url}/error?code=400", status_code=302)
    if not code:
        return RedirectResponse(url=f"{settings.frontend_url}/error?code=500", status_code=302)

    with httpx.Client() as client:
        token_resp = client.post(
            GITHUB_TOKEN_URL,
            data={
                "client_id": settings.github_client_id,
                "client_secret": settings.github_client_secret,
                "code": code,
            },
            headers={"Accept": "application/json"},
        )
        token_data = token_resp.json()
        access_token = token_data.get("access_token")
        if not access_token:
            return RedirectResponse(url=f"{settings.frontend_url}/error?code=500", status_code=302)

        auth_headers = {"Authorization": f"Bearer {access_token}", "Accept": "application/json"}
        user_resp = client.get(GITHUB_USER_URL, headers=auth_headers)
        user_info = user_resp.json()

        # name が null の場合は login を使う
        name = user_info.get("name") or user_info.get("login", "")

        # email が null の場合は /user/emails から primary を取得
        email = user_info.get("email")
        if not email:
            emails_resp = client.get(GITHUB_EMAIL_URL, headers=auth_headers)
            emails = emails_resp.json()
            primary = next((e for e in emails if e.get("primary")), None)
            email = primary["email"] if primary else ""

    dto = AuthLoginDto(
        provider=2,  # Provider::Github
        provider_id=str(user_info["id"]),
        name=name,
        email=email,
        avatar=user_info.get("avatar_url"),
        invitation_token=invitation_token,
    )
    try:
        staff = interactor.login(dto)
    except Exception as e:
        code_ = 403 if hasattr(e, "status_code") and e.status_code == 403 else 500
        return RedirectResponse(url=f"{settings.frontend_url}/error?code={code_}", status_code=302)

    max_age = settings.staff_cookie_lifetime * 60
    redirect = RedirectResponse(url=f"{settings.frontend_url}/clients", status_code=302)
    redirect.set_cookie("staff_id", str(staff.id), max_age=max_age, httponly=True, samesite="lax")
    return redirect
