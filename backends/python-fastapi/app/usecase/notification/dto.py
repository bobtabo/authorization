from dataclasses import dataclass, field
from typing import Optional


@dataclass
class NotificationStoreDto:
    title: str
    body: Optional[str] = None


@dataclass
class NotificationBulkReadDto:
    executor_id: int
    ids: list[int] = field(default_factory=list)
    all_flag: bool = False


@dataclass
class NotificationPatchDto:
    notification_id: int
    read: Optional[bool] = None
    title: Optional[str] = None
    message: Optional[str] = None
