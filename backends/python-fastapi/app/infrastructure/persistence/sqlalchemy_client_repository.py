"""
クライアントリポジトリ SQLAlchemy 実装モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from typing import Optional

from sqlalchemy import or_
from sqlalchemy.orm import Session
from datetime import datetime, timezone

from app.domain.client.entity import Client
from app.domain.client.condition import ClientCondition
from app.domain.client.repository import ClientRepository
from app.exceptions import conflict
from app.infrastructure.model.model import ClientModel
from app.support.assign import assign


def _to_entity(m: ClientModel) -> Client:
    return assign(Client(), m)


class SqlAlchemyClientRepository(ClientRepository):
    """ClientRepository の SQLAlchemy 実装。

    Attributes:
        db: SQLAlchemy セッション
    """

    def __init__(self, db: Session):
        """初期化します。

        Args:
            db: SQLAlchemy セッション
        """
        self.db = db

    def find_all_clients(self, cond: ClientCondition) -> list[Client]:
        """検索条件に合致するクライアントエンティティを返します。

        Args:
            cond: 検索条件

        Returns:
            クライアントエンティティのリスト
        """
        q = self.db.query(ClientModel)
        if cond.keyword:
            like = f"%{cond.keyword}%"
            q = q.filter(or_(ClientModel.name.like(like), ClientModel.identifier.like(like)))
        if cond.status is not None:
            q = q.filter(ClientModel.status == cond.status)
        return [_to_entity(m) for m in q.order_by(ClientModel.id).all()]

    def find_client_by_id(self, client_id: int) -> Optional[Client]:
        """IDでクライアントエンティティを返します。存在しない場合は None を返します。

        Args:
            client_id: クライアントID

        Returns:
            クライアントエンティティ、または None
        """
        m = self.db.query(ClientModel).filter(ClientModel.id == client_id).first()
        return _to_entity(m) if m else None

    def find_client_by_token(self, token: str) -> Optional[Client]:
        """アクセストークンでアクティブなクライアントエンティティを返します。

        Args:
            token: アクセストークン

        Returns:
            クライアントエンティティ、または None
        """
        m = self.db.query(ClientModel).filter(
            ClientModel.token == token,
            ClientModel.status == 2,  # Active のみ
            ClientModel.deleted_at.is_(None),
        ).first()
        return _to_entity(m) if m else None

    def find_client_by_identifier(self, identifier: str) -> Optional[Client]:
        """識別子でクライアントエンティティを返します。

        Args:
            identifier: クライアント識別子

        Returns:
            クライアントエンティティ、または None
        """
        m = self.db.query(ClientModel).filter(
            ClientModel.identifier == identifier,
            ClientModel.deleted_at.is_(None),
        ).first()
        return _to_entity(m) if m else None

    def save_client(self, client: Client) -> Client:
        """クライアントエンティティを保存（新規作成または更新）して返します。

        Args:
            client: 保存するクライアントエンティティ

        Returns:
            保存済みクライアントエンティティ

        Raises:
            ValueError: 更新対象のクライアントが存在しない場合
        """
        if client.id:
            m = self.db.query(ClientModel).filter(ClientModel.id == client.id).first()
            if m is None:
                raise ValueError(f"Client {client.id} not found")
            if m.version != client.version:
                raise conflict("optimistic_lock")
            m.version += 1
        else:
            m = ClientModel()

        m.name = client.name
        m.identifier = client.identifier
        m.post_code = client.post_code
        m.pref = client.pref
        m.city = client.city
        m.address = client.address
        m.building = client.building
        m.tel = client.tel
        m.email = client.email
        m.status = client.status
        m.token = client.token
        m.public_key = client.public_key
        m.private_key = client.private_key
        m.fingerprint = client.fingerprint
        m.started_at = client.started_at
        m.stopped_at = client.stopped_at
        m.deleted_at = client.deleted_at
        if not client.id:
            m.created_by = client.executor_id
        m.updated_by = client.executor_id

        self.db.add(m)
        self.db.flush()
        self.db.refresh(m)
        return _to_entity(m)

    def soft_delete_client(self, client: Client) -> None:
        """クライアントを論理削除します。

        Args:
            client: 削除対象のクライアントエンティティ
        """
        m = self.db.query(ClientModel).filter(ClientModel.id == client.id).first()
        if m:
            if m.version != client.version:
                raise conflict("optimistic_lock")
            m.version += 1
            m.deleted_at = datetime.now(timezone.utc)
            self.db.flush()
