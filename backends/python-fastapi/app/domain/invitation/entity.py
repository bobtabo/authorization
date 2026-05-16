"""
招待ドメイン エンティティモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import dataclass
from datetime import datetime
from typing import Optional


@dataclass
class Invitation:
    """招待のドメインエンティティ（SQLAlchemy タグなし）。"""
    id: int = 0
    token: str = ""
    role: int = 2
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
