from typing import Optional
from app.exceptions import not_found
from app.models.models import Notification
from app.repositories.notification_repo import NotificationRepository, NotificationPage
from app.repositories.staff_repo import StaffRepository


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


class NotificationService:
    def __init__(self, notif_repo: NotificationRepository, staff_repo: StaffRepository):
        self.notif_repo = notif_repo
        self.staff_repo = staff_repo

    def list_page(self, staff_id: int, cursor: Optional[str], limit: int) -> NotificationPage:
        return self.notif_repo.list_page(staff_id, cursor, limit)

    def counts(self, staff_id: int) -> tuple[int, int]:
        return self.notif_repo.counts(staff_id)

    def bulk_mark_read(self, executor_id: int, ids: list[int], all_flag: bool) -> int:
        return self.notif_repo.bulk_mark_read(executor_id, ids, all_flag)

    def fan_out(self, title: str, body: Optional[str]) -> None:
        staffs = self.staff_repo.find_all_active()
        for staff in staffs:
            n = Notification(staff_id=staff.id, title=title, message=body or "", read=False)
            self.notif_repo.store(n)

    def patch(self, nid: int, data: dict) -> None:
        notif = self.notif_repo.find_by_id(nid)
        if notif is None:
            raise not_found("notification_not_found")
        self.notif_repo.patch(notif, data)
