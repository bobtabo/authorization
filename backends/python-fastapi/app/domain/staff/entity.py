from dataclasses import dataclass
from datetime import datetime
from typing import Optional


@dataclass
class Staff:
    """スタッフのドメインエンティティ（SQLAlchemy タグなし）。"""
    id: int = 0
    name: str = ""
    email: str = ""
    provider: int = 0
    provider_id: str = ""
    avatar: Optional[str] = None
    role: int = 0
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    deleted_at: Optional[datetime] = None
