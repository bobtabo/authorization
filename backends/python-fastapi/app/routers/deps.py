from typing import Optional
from fastapi import Cookie, Depends, Header
from sqlalchemy.orm import Session
from app.config.settings import get_settings, Settings
from app.exceptions import unauthorized
from app.infrastructure.db import get_db
from app.infrastructure.redis_client import get_redis
from app.infrastructure.persistence.sqlalchemy_client_repository import SqlAlchemyClientRepository
from app.infrastructure.persistence.sqlalchemy_staff_repository import SqlAlchemyStaffRepository
from app.infrastructure.persistence.sqlalchemy_invitation_repository import SqlAlchemyInvitationRepository
from app.infrastructure.persistence.sqlalchemy_notification_repository import SqlAlchemyNotificationRepository
from app.infrastructure.cache.gate_cache_repository import GateCacheRepository
from app.domain.client.repository import ClientRepository
from app.domain.staff.repository import StaffRepository
from app.usecase.auth.interactor import AuthInteractor
from app.usecase.client.interactor import ClientInteractor
from app.usecase.staff.interactor import StaffInteractor
from app.usecase.invitation.interactor import InvitationInteractor
from app.usecase.gate.interactor import GateInteractor
from app.usecase.notification.interactor import NotificationInteractor
import redis as redis_lib


def get_redis_client() -> redis_lib.Redis:
    return get_redis()


def get_client_repo(db: Session = Depends(get_db)) -> ClientRepository:
    return SqlAlchemyClientRepository(db)


def get_staff_repo(db: Session = Depends(get_db)) -> StaffRepository:
    return SqlAlchemyStaffRepository(db)


def get_auth_interactor(staff_repo: StaffRepository = Depends(get_staff_repo)) -> AuthInteractor:
    return AuthInteractor(staff_repo)


def get_client_interactor(client_repo: ClientRepository = Depends(get_client_repo)) -> ClientInteractor:
    return ClientInteractor(client_repo)


def get_staff_interactor(staff_repo: StaffRepository = Depends(get_staff_repo)) -> StaffInteractor:
    return StaffInteractor(staff_repo)


def get_invitation_interactor(
    db: Session = Depends(get_db),
    settings: Settings = Depends(get_settings),
) -> InvitationInteractor:
    repo = SqlAlchemyInvitationRepository(db, settings.frontend_url)
    return InvitationInteractor(repo)


def get_gate_interactor(
    client_repo: ClientRepository = Depends(get_client_repo),
    rdb: redis_lib.Redis = Depends(get_redis_client),
) -> GateInteractor:
    cache_repo = GateCacheRepository(rdb)
    return GateInteractor(client_repo, cache_repo)


def get_notification_interactor(
    db: Session = Depends(get_db),
    staff_repo: StaffRepository = Depends(get_staff_repo),
) -> NotificationInteractor:
    notif_repo = SqlAlchemyNotificationRepository(db)
    return NotificationInteractor(notif_repo, staff_repo)


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
    client_interactor: ClientInteractor = Depends(get_client_interactor),
):
    client = client_interactor.authenticate_by_token(token)
    if client is None:
        raise unauthorized("invalid_token")
    return client
