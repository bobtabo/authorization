"""
スタッフユースケース Dto モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import dataclass
from typing import Optional


@dataclass
class StaffLoginDto:
    """スタッフログイン Dto。"""

    provider: int
    provider_id: str
    name: str
    email: str
    avatar: Optional[str] = None


@dataclass
class StaffUpdateRoleDto:
    """スタッフロール更新 Dto。"""

    staff_id: int
    role: int
    executor_id: int


@dataclass
class StaffDestroyDto:
    """スタッフ論理削除 Dto。"""

    staff_id: int
    executor_id: int
