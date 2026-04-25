"""
招待リポジトリ SQLAlchemy 実装モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
import secrets
from typing import Optional

from sqlalchemy.orm import Session

from app.domain.invitation.value_objects import InvitationVo
from app.domain.invitation.repository import InvitationRepository
from app.infrastructure.model.model import InvitationModel


def _build_vo(token: str, frontend_url: str) -> InvitationVo:
    """トークンと URL から InvitationVo を構築します。

    Args:
        token: 招待トークン
        frontend_url: フロントエンドのベース URL

    Returns:
        InvitationVo インスタンス
    """
    url = f"{frontend_url}/invitation/{token}"
    display_url = url.replace("https://", "").replace("http://", "")
    return InvitationVo(token=token, url=url, display_url=display_url)



class SqlAlchemyInvitationRepository(InvitationRepository):
    """InvitationRepository の SQLAlchemy 実装。

    Attributes:
        db: SQLAlchemy セッション
        frontend_url: フロントエンドのベース URL
    """

    def __init__(self, db: Session, frontend_url: str):
        """初期化します。

        Args:
            db: SQLAlchemy セッション
            frontend_url: フロントエンドのベース URL（招待 URL 生成に使用）
        """
        self.db = db
        self.frontend_url = frontend_url

    def get_current(self) -> Optional[InvitationVo]:
        """最新の招待情報のバリューオブジェクトを返します。

        Returns:
            InvitationVo、または None
        """
        m = self.db.query(InvitationModel).order_by(InvitationModel.id.desc()).first()
        if m is None:
            return None
        return _build_vo(m.token, self.frontend_url)

    def issue(self) -> InvitationVo:
        """新しい招待トークンを生成して保存し、バリューオブジェクトを返します。

        Returns:
            InvitationVo インスタンス
        """
        token = secrets.token_hex(16)
        m = InvitationModel(token=token)
        self.db.add(m)
        self.db.flush()
        return _build_vo(token, self.frontend_url)

    def find_by_token(self, token: str) -> Optional[InvitationVo]:
        """トークンで招待情報のバリューオブジェクトを返します。

        Args:
            token: 招待トークン

        Returns:
            InvitationVo、または None
        """
        m = self.db.query(InvitationModel).filter(InvitationModel.token == token).first()
        if m is None:
            return None
        return _build_vo(m.token, self.frontend_url)
