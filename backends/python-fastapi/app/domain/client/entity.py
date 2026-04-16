from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional


@dataclass
class Client:
    """クライアントのドメインエンティティ（SQLAlchemy タグなし）。"""
    id: int = 0
    name: str = ""
    identifier: str = ""
    post_code: str = ""
    pref: str = ""
    city: str = ""
    address: str = ""
    building: str = ""
    tel: str = ""
    email: str = ""
    status: int = 0
    token: Optional[str] = None
    public_key: Optional[str] = None
    private_key: Optional[str] = None
    fingerprint: Optional[str] = None
    started_at: Optional[datetime] = None
    stopped_at: Optional[datetime] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    deleted_at: Optional[datetime] = None
