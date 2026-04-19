from dataclasses import dataclass
from datetime import datetime
from typing import Optional


@dataclass
class Invitation:
    """招待のドメインエンティティ（SQLAlchemy タグなし）。"""
    id: int = 0
    token: str = ""
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
