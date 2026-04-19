from abc import ABC, abstractmethod
from typing import Optional
from app.domain.notification.entity import Notification
from app.domain.notification.value_objects import NotificationPage


class NotificationRepository(ABC):
    """通知の永続化インターフェース。"""

    @abstractmethod
    def list_page(self, staff_id: int, cursor: Optional[str], limit: int) -> NotificationPage:
        ...

    @abstractmethod
    def counts(self, staff_id: int) -> tuple[int, int]:
        ...

    @abstractmethod
    def bulk_mark_read(self, executor_id: int, ids: list[int], all_flag: bool) -> int:
        ...

    @abstractmethod
    def store(self, notification: Notification) -> Notification:
        ...

    @abstractmethod
    def patch(self, notification: Notification, data: dict) -> None:
        ...

    @abstractmethod
    def find_by_id(self, nid: int) -> Optional[Notification]:
        ...
