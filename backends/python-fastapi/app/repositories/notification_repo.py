import base64
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Optional
from sqlalchemy.orm import Session
from app.models.models import Notification


@dataclass
class NotificationPage:
    items: list[Notification]
    next_cursor: Optional[str]


def _encode_cursor(ts: int, nid: int) -> str:
    raw = f"{ts},{nid}"
    return base64.b64encode(raw.encode()).decode()


def _decode_cursor(cursor: str) -> tuple[int, int]:
    raw = base64.b64decode(cursor.encode()).decode()
    ts_str, id_str = raw.split(",", 1)
    return int(ts_str), int(id_str)


class NotificationRepository:
    def __init__(self, db: Session):
        self.db = db

    def list_page(self, staff_id: int, cursor: Optional[str], limit: int) -> NotificationPage:
        q = self.db.query(Notification).filter(Notification.staff_id == staff_id)
        if cursor:
            ts, nid = _decode_cursor(cursor)
            cur_dt = datetime.fromtimestamp(ts, tz=timezone.utc)
            q = q.filter(
                (Notification.created_at < cur_dt) |
                ((Notification.created_at == cur_dt) & (Notification.id < nid))
            )
        q = q.order_by(Notification.created_at.desc(), Notification.id.desc())
        items = q.limit(limit + 1).all()

        next_cursor = None
        if len(items) > limit:
            items = items[:limit]
            last = items[-1]
            ts = int(last.created_at.replace(tzinfo=timezone.utc).timestamp())
            next_cursor = _encode_cursor(ts, last.id)

        return NotificationPage(items=items, next_cursor=next_cursor)

    def counts(self, staff_id: int) -> tuple[int, int]:
        total = self.db.query(Notification).filter(Notification.staff_id == staff_id).count()
        unread = self.db.query(Notification).filter(
            Notification.staff_id == staff_id, Notification.read == False  # noqa: E712
        ).count()
        return unread, total

    def bulk_mark_read(self, executor_id: int, ids: list[int], all_flag: bool) -> int:
        q = self.db.query(Notification).filter(Notification.staff_id == executor_id)
        if not all_flag:
            q = q.filter(Notification.id.in_(ids))
        count = q.filter(Notification.read == False).count()  # noqa: E712
        q.filter(Notification.read == False).update({"read": True}, synchronize_session=False)  # noqa: E712
        self.db.commit()
        return count

    def store(self, notification: Notification) -> Notification:
        self.db.add(notification)
        self.db.commit()
        self.db.refresh(notification)
        return notification

    def patch(self, notification: Notification, data: dict) -> None:
        allowed = {"read", "title", "body"}
        for key, val in data.items():
            if key in allowed:
                setattr(notification, key, val)
        self.db.commit()

    def find_by_id(self, nid: int) -> Optional[Notification]:
        return self.db.query(Notification).filter(Notification.id == nid).first()
