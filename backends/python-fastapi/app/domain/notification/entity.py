from dataclasses import dataclass
from datetime import datetime
from typing import Optional


@dataclass
class Notification:
    """通知のドメインエンティティ（SQLAlchemy タグなし）。"""
    id: int = 0
    staff_id: int = 0
    message_type: int = 1
    title: str = ""
    message: str = ""
    url: Optional[str] = None
    read: bool = False
    created_at: Optional[datetime] = None
    created_by: int = 0
    updated_at: Optional[datetime] = None
    updated_by: int = 0
    version: int = 0
