from typing import Optional
from sqlalchemy import or_
from sqlalchemy.orm import Session
from app.models.models import Client


class ClientRepository:
    def __init__(self, db: Session):
        self.db = db

    def find_all(self, keyword: Optional[str] = None, status: Optional[int] = None) -> list[Client]:
        q = self.db.query(Client)
        if keyword:
            like = f"%{keyword}%"
            q = q.filter(or_(Client.name.like(like), Client.identifier.like(like)))
        if status is not None:
            q = q.filter(Client.status == status)
        return q.order_by(Client.id).all()

    def find_by_id(self, client_id: int) -> Optional[Client]:
        return self.db.query(Client).filter(Client.id == client_id).first()

    def find_by_access_token(self, token: str) -> Optional[Client]:
        return self.db.query(Client).filter(
            Client.token == token,
            Client.status == 2,  # Active のみ
            Client.deleted_at.is_(None),
        ).first()

    def find_by_identifier(self, identifier: str) -> Optional[Client]:
        return self.db.query(Client).filter(Client.identifier == identifier, Client.deleted_at.is_(None)).first()

    def save(self, client: Client) -> Client:
        self.db.add(client)
        self.db.commit()
        self.db.refresh(client)
        return client

    def soft_delete(self, client: Client) -> None:
        from datetime import datetime, timezone
        client.deleted_at = datetime.now(timezone.utc)
        self.db.commit()
