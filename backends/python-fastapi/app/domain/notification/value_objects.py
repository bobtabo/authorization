from dataclasses import dataclass, field
from typing import Optional
from app.domain.notification.entity import Notification


@dataclass
class NotificationPage:
    """通知ページングバリューオブジェクト。"""
    items: list[Notification]
    next_cursor: Optional[str]


@dataclass(frozen=True)
class NotificationCountsVo:
    """通知カウントバリューオブジェクト。"""
    unread: int
    total: int


@dataclass(frozen=True)
class NotificationPatchVo:
    """通知パッチバリューオブジェクト。"""
    read: Optional[bool] = None
    title: Optional[str] = None
    message: Optional[str] = None


@dataclass(frozen=True)
class NotificationBulkPatchVo:
    """通知一括既読バリューオブジェクト。"""
    executor_id: int
    ids: list[int] = field(default_factory=list)
    all_flag: bool = False
