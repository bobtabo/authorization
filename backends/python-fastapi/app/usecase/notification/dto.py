"""
通知ユースケース Dto モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class NotificationStoreDto:
    """通知登録 Dto。"""

    title: str
    body: Optional[str] = None


@dataclass
class NotificationBulkReadDto:
    """通知一括既読 Dto。"""

    executor_id: int
    ids: list[int] = field(default_factory=list)
    all_flag: bool = False


@dataclass
class NotificationPatchDto:
    """通知更新 Dto。"""

    notification_id: int
    read: Optional[bool] = None
    title: Optional[str] = None
    message: Optional[str] = None
