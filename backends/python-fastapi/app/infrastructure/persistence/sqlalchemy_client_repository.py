from typing import Optional
from sqlalchemy import or_
from sqlalchemy.orm import Session
from datetime import datetime, timezone

from app.domain.client.entity import Client
from app.domain.client.condition import ClientCondition
from app.domain.client.repository import ClientRepository
from app.infrastructure.model.model import ClientModel


def _to_entity(m: ClientModel) -> Client:
    return Client(
        id=m.id,
        name=m.name,
        identifier=m.identifier,
        post_code=m.post_code,
        pref=m.pref,
        city=m.city,
        address=m.address,
        building=m.building,
        tel=m.tel,
        email=m.email,
        status=m.status,
        token=m.token,
        public_key=m.public_key,
        private_key=m.private_key,
        fingerprint=m.fingerprint,
        started_at=m.started_at,
        stopped_at=m.stopped_at,
        created_at=m.created_at,
        updated_at=m.updated_at,
        deleted_at=m.deleted_at,
    )


class SqlAlchemyClientRepository(ClientRepository):
    """ClientRepository の SQLAlchemy 実装。"""

    def __init__(self, db: Session):
        self.db = db

    def find_all_clients(self, cond: ClientCondition) -> list[Client]:
        q = self.db.query(ClientModel)
        if cond.keyword:
            like = f"%{cond.keyword}%"
            q = q.filter(or_(ClientModel.name.like(like), ClientModel.identifier.like(like)))
        if cond.status is not None:
            q = q.filter(ClientModel.status == cond.status)
        return [_to_entity(m) for m in q.order_by(ClientModel.id).all()]

    def find_client_by_id(self, client_id: int) -> Optional[Client]:
        m = self.db.query(ClientModel).filter(ClientModel.id == client_id).first()
        return _to_entity(m) if m else None

    def find_client_by_token(self, token: str) -> Optional[Client]:
        m = self.db.query(ClientModel).filter(
            ClientModel.token == token,
            ClientModel.status == 2,  # Active のみ
            ClientModel.deleted_at.is_(None),
        ).first()
        return _to_entity(m) if m else None

    def find_client_by_identifier(self, identifier: str) -> Optional[Client]:
        m = self.db.query(ClientModel).filter(
            ClientModel.identifier == identifier,
            ClientModel.deleted_at.is_(None),
        ).first()
        return _to_entity(m) if m else None

    def save_client(self, client: Client) -> Client:
        if client.id:
            m = self.db.query(ClientModel).filter(ClientModel.id == client.id).first()
            if m is None:
                raise ValueError(f"Client {client.id} not found")
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
        self.db.commit()
        self.db.refresh(m)
        return _to_entity(m)

    def soft_delete_client(self, client: Client) -> None:
        m = self.db.query(ClientModel).filter(ClientModel.id == client.id).first()
        if m:
            m.deleted_at = datetime.now(timezone.utc)
            self.db.commit()
