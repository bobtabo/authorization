import secrets
from dataclasses import dataclass
from typing import Optional
from sqlalchemy.orm import Session
from app.models.models import Invitation


@dataclass
class InvitationResult:
    token: str
    url: str
    display_url: str


def _build_result(token: str, frontend_url: str) -> InvitationResult:
    url = f"{frontend_url}/invitation/{token}"
    display_url = url.replace("https://", "").replace("http://", "")
    return InvitationResult(token=token, url=url, display_url=display_url)


class InvitationRepository:
    def __init__(self, db: Session, frontend_url: str):
        self.db = db
        self.frontend_url = frontend_url

    def get_current(self) -> Optional[InvitationResult]:
        inv = self.db.query(Invitation).order_by(Invitation.id.desc()).first()
        if inv is None:
            return None
        return _build_result(inv.token, self.frontend_url)

    def issue(self) -> InvitationResult:
        token = secrets.token_hex(16)
        inv = Invitation(token=token)
        self.db.add(inv)
        self.db.commit()
        return _build_result(token, self.frontend_url)

    def find_by_token(self, token: str) -> Optional[Invitation]:
        return self.db.query(Invitation).filter(Invitation.token == token).first()
