"""
JWT 履歴リポジトリ SQLAlchemy 実装モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from datetime import datetime

from sqlalchemy.orm import Session

from app.infrastructure.model.model import JwtHistoryModel


class SqlAlchemyJwtHistoryRepository:
    """JwtHistoryRepository の SQLAlchemy 実装。"""

    def __init__(self, db: Session):
        self.db = db

    def find_by_client_id(self, client_id: int) -> list[JwtHistoryModel]:
        return (
            self.db.query(JwtHistoryModel)
            .filter(JwtHistoryModel.client_id == client_id, JwtHistoryModel.deleted_at.is_(None))
            .order_by(JwtHistoryModel.issue_at.desc())
            .all()
        )

    def save(self, client_id: int, member_id: str, issue_at: datetime, jwt: str) -> None:
        now = datetime.now()
        record = JwtHistoryModel(
            client_id=client_id,
            member_id=member_id,
            issue_at=issue_at,
            jwt=jwt,
            created_at=now,
            created_by=0,
            updated_at=now,
            updated_by=0,
            version=1,
        )
        self.db.add(record)
        self.db.commit()
