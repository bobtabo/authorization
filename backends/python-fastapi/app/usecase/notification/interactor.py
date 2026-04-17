from typing import Optional

from app.domain.notification.entity import Notification
from app.domain.notification.value_objects import NotificationPage
from app.domain.notification.repository import NotificationRepository
from app.domain.staff.repository import StaffRepository
from app.exceptions import not_found
from app.usecase.notification.dto import NotificationStoreDto


def map_notification(n: Notification) -> dict:
    return {
        "id": n.id,
        "staff_id": n.staff_id,
        "message_type": n.message_type,
        "title": n.title,
        "message": n.message,
        "url": n.url,
        "read": n.read,
        "created_at": n.created_at.strftime("%Y-%m-%d %H:%M") if n.created_at else None,
        "updated_at": n.updated_at.strftime("%Y-%m-%d %H:%M") if n.updated_at else None,
    }


class NotificationInteractor:
    """通知のユースケース実装。"""

    def __init__(self, notif_repo: NotificationRepository, staff_repo: StaffRepository):
        self.notif_repo = notif_repo
        self.staff_repo = staff_repo

    def list_page(self, staff_id: int, cursor: Optional[str], limit: int) -> NotificationPage:
        return self.notif_repo.list_page(staff_id, cursor, limit)

    def counts(self, staff_id: int) -> tuple[int, int]:
        return self.notif_repo.counts(staff_id)

    def bulk_mark_read(self, staff_id: int) -> int:
        return self.notif_repo.bulk_mark_read(staff_id, [], True)

    def mark_read(self, notification_id: int) -> None:
        notif = self.notif_repo.find_by_id(notification_id)
        if notif is None:
            raise not_found("notification_not_found")
        self.notif_repo.patch(notif, {"read": True})

    def fan_out(self, title: str, body: Optional[str]) -> None:
        staffs = self.staff_repo.find_all_active_staffs()
        for staff in staffs:
            n = Notification(staff_id=staff.id, title=title, message=body or "", read=False)
            self.notif_repo.store(n)
