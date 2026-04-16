from dataclasses import dataclass
from typing import Optional


@dataclass
class ClientCondition:
    """クライアント検索条件。"""
    keyword: Optional[str] = None
    status: Optional[int] = None
