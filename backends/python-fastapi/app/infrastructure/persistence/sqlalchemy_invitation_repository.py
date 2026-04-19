import secrets
from typing import Optional
from sqlalchemy.orm import Session

from app.domain.invitation.entity import Invitation
from app.domain.invitation.value_objects import InvitationVo
from app.domain.invitation.repository import InvitationRepository
from app.infrastructure.model.model import InvitationModel


def _build_vo(token: str, frontend_url: str) -> InvitationVo:
    url = f"{frontend_url}/invitation/{token}"
    display_url = url.replace("https://", "").replace("http://", "")
    return InvitationVo(token=token, url=url, display_url=display_url)


def _to_entity(m: InvitationModel) -> Invitation:
    return Invitation(
        id=m.id,
        token=m.token,
        created_at=m.created_at,
        updated_at=m.updated_at,
    )


class SqlAlchemyInvitationRepository(InvitationRepository):
    """InvitationRepository の SQLAlchemy 実装。"""

    def __init__(self, db: Session, frontend_url: str):
        self.db = db
        self.frontend_url = frontend_url

    def get_current(self) -> Optional[InvitationVo]:
        m = self.db.query(InvitationModel).order_by(InvitationModel.id.desc()).first()
        if m is None:
            return None
        return _build_vo(m.token, self.frontend_url)

    def issue(self) -> InvitationVo:
        token = secrets.token_hex(16)
        m = InvitationModel(token=token)
        self.db.add(m)
        self.db.commit()
        return _build_vo(token, self.frontend_url)

    def find_by_token(self, token: str) -> Optional[Invitation]:
        m = self.db.query(InvitationModel).filter(InvitationModel.token == token).first()
        return _to_entity(m) if m else None
