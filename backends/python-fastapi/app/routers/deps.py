from typing import Optional
from fastapi import Cookie, Depends, Header
from sqlalchemy.orm import Session
from app.config.settings import get_settings, Settings
from app.exceptions import unauthorized
from app.infrastructure.db import get_db
from app.infrastructure.redis_client import get_redis
from app.repositories.client_repo import ClientRepository
from app.repositories.gate_cache_repo import GateCacheRepository
from app.repositories.invitation_repo import InvitationRepository
from app.repositories.notification_repo import NotificationRepository
from app.repositories.staff_repo import StaffRepository
from app.services.auth_service import AuthService
from app.services.client_service import ClientService
from app.services.gate_service import GateService
from app.services.invitation_service import InvitationService
from app.services.notification_service import NotificationService
from app.services.staff_service import StaffService
import redis as redis_lib


def get_redis_client() -> redis_lib.Redis:
    return get_redis()


def get_staff_repo(db: Session = Depends(get_db)) -> StaffRepository:
    return StaffRepository(db)


def get_client_repo(db: Session = Depends(get_db)) -> ClientRepository:
    return ClientRepository(db)


def get_auth_service(staff_repo: StaffRepository = Depends(get_staff_repo)) -> AuthService:
    return AuthService(staff_repo)


def get_client_service(client_repo: ClientRepository = Depends(get_client_repo)) -> ClientService:
    return ClientService(client_repo)


def get_staff_service(staff_repo: StaffRepository = Depends(get_staff_repo)) -> StaffService:
    return StaffService(staff_repo)


def get_invitation_service(
    db: Session = Depends(get_db),
    settings: Settings = Depends(get_settings),
) -> InvitationService:
    repo = InvitationRepository(db, settings.frontend_url)
    return InvitationService(repo)


def get_gate_service(
    client_repo: ClientRepository = Depends(get_client_repo),
    rdb: redis_lib.Redis = Depends(get_redis_client),
) -> GateService:
    cache_repo = GateCacheRepository(rdb)
    return GateService(client_repo, cache_repo)


def get_notification_service(
    db: Session = Depends(get_db),
    staff_repo: StaffRepository = Depends(get_staff_repo),
) -> NotificationService:
    notif_repo = NotificationRepository(db)
    return NotificationService(notif_repo, staff_repo)


def get_staff_id_from_cookie(cookie_sid: Optional[str] = Cookie(default=None, alias="staff_id")) -> int:
    if not cookie_sid:
        return 0
    try:
        return int(cookie_sid)
    except ValueError:
        return 0


def require_staff_id(staff_id: int = Depends(get_staff_id_from_cookie)) -> int:
    if staff_id == 0:
        raise unauthorized("unauthenticated")
    return staff_id


def get_bearer_token(authorization: Optional[str] = Header(default=None)) -> str:
    if not authorization or not authorization.startswith("Bearer "):
        raise unauthorized("token_required")
    return authorization.removeprefix("Bearer ")


def require_client_token(
    token: str = Depends(get_bearer_token),
    client_service: ClientService = Depends(get_client_service),
):
    client = client_service.authenticate_by_token(token)
    if client is None:
        raise unauthorized("invalid_token")
    return client
