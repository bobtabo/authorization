"""
スタッフドメイン 検索条件モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class StaffCondition:
    """スタッフ検索条件。"""
    keyword: Optional[str] = None
    roles: list[int] = field(default_factory=list)
    offset: int = 0
    limit: int = 10
    sort: Optional[str] = None
    sort_type: Optional[str] = None
