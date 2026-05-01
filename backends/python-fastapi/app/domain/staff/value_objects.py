"""
スタッフドメイン バリューオブジェクトモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import dataclass
from datetime import datetime
from typing import Optional


@dataclass(frozen=True)
class StaffListItem:
    """スタッフ一覧レスポンス用バリューオブジェクト。"""

    id: int
    name: str
    email: str
    role: int
    status: int  # 0=削除済み, 1=有効
    created_at: Optional[datetime]
    updated_at: Optional[datetime]


@dataclass(frozen=True)
class StaffVo:
    """スタッフ詳細レスポンス用バリューオブジェクト。"""

    id: int
    name: str
    avatar: Optional[str]
    role: int
