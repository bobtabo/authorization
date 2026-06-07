"""
JWT 履歴リポジトリ SQLAlchemy 実装モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from datetime import datetime, timezone

from sqlalchemy import asc, desc, func
from sqlalchemy.orm import Session

from app.infrastructure.model.model import JwtHistoryModel

_ALLOWED_SORT = {"issue_at", "member_id"}


class SqlAlchemyJwtHistoryRepository:
    """JwtHistoryRepository の SQLAlchemy 実装。"""

    def __init__(self, db: Session):
        self.db = db

    def count_by_client_id(self, client_id: int) -> int:
        return (
            self.db.query(func.count(JwtHistoryModel.id))
            .filter(JwtHistoryModel.client_id == client_id, JwtHistoryModel.deleted_at.is_(None))
            .scalar() or 0
        )

    def find_by_condition(
        self,
        client_id: int,
        offset: int = 0,
        limit: int = 20,
        sort: str = "issue_at",
        sort_type: str = "desc",
    ) -> list[JwtHistoryModel]:
        sort_col = sort if sort in _ALLOWED_SORT else "issue_at"
        col = getattr(JwtHistoryModel, sort_col)
        order = asc(col) if sort_type.lower() == "asc" else desc(col)
        return (
            self.db.query(JwtHistoryModel)
            .filter(JwtHistoryModel.client_id == client_id, JwtHistoryModel.deleted_at.is_(None))
            .order_by(order)
            .limit(max(1, limit))
            .offset(offset)
            .all()
        )

    def save(self, client_id: int, member_id: str, issue_at: datetime, jwt: str) -> None:
        now = datetime.now(timezone.utc)
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
        try:
            self.db.add(record)
            self.db.commit()
        except Exception:
            self.db.rollback()
            raise
