import base64
from datetime import datetime, timezone
from typing import Optional
from sqlalchemy.orm import Session

from app.domain.notification.entity import Notification
from app.domain.notification.value_objects import NotificationPage
from app.domain.notification.repository import NotificationRepository
from app.infrastructure.model.model import NotificationModel


def _encode_cursor(ts: int, nid: int) -> str:
    raw = f"{ts},{nid}"
    return base64.b64encode(raw.encode()).decode()


def _decode_cursor(cursor: str) -> tuple[int, int]:
    raw = base64.b64decode(cursor.encode()).decode()
    ts_str, id_str = raw.split(",", 1)
    return int(ts_str), int(id_str)


def _to_entity(m: NotificationModel) -> Notification:
    return Notification(
        id=m.id,
        staff_id=m.staff_id,
        message_type=m.message_type,
        title=m.title,
        message=m.message,
        url=m.url,
        read=m.read,
        created_at=m.created_at,
        created_by=m.created_by,
        updated_at=m.updated_at,
        updated_by=m.updated_by,
        version=m.version,
    )


class SqlAlchemyNotificationRepository(NotificationRepository):
    """NotificationRepository の SQLAlchemy 実装。"""

    def __init__(self, db: Session):
        self.db = db

    def list_page(self, staff_id: int, cursor: Optional[str], limit: int) -> NotificationPage:
        q = self.db.query(NotificationModel).filter(NotificationModel.staff_id == staff_id)
        if cursor:
            ts, nid = _decode_cursor(cursor)
            cur_dt = datetime.fromtimestamp(ts, tz=timezone.utc)
            q = q.filter(
                (NotificationModel.created_at < cur_dt) |
                ((NotificationModel.created_at == cur_dt) & (NotificationModel.id < nid))
            )
        q = q.order_by(NotificationModel.created_at.desc(), NotificationModel.id.desc())
        rows = q.limit(limit + 1).all()

        next_cursor = None
        if len(rows) > limit:
            rows = rows[:limit]
            last = rows[-1]
            ts = int(last.created_at.replace(tzinfo=timezone.utc).timestamp())
            next_cursor = _encode_cursor(ts, last.id)

        return NotificationPage(items=[_to_entity(m) for m in rows], next_cursor=next_cursor)

    def counts(self, staff_id: int) -> tuple[int, int]:
        total = self.db.query(NotificationModel).filter(NotificationModel.staff_id == staff_id).count()
        unread = self.db.query(NotificationModel).filter(
            NotificationModel.staff_id == staff_id,
            NotificationModel.read == False,  # noqa: E712
        ).count()
        return unread, total

    def bulk_mark_read(self, executor_id: int, ids: list[int], all_flag: bool) -> int:
        q = self.db.query(NotificationModel).filter(NotificationModel.staff_id == executor_id)
        if not all_flag:
            q = q.filter(NotificationModel.id.in_(ids))
        count = q.filter(NotificationModel.read == False).count()  # noqa: E712
        q.filter(NotificationModel.read == False).update({"read": True}, synchronize_session=False)  # noqa: E712
        self.db.commit()
        return count

    def store(self, notification: Notification) -> Notification:
        m = NotificationModel(
            staff_id=notification.staff_id,
            message_type=notification.message_type,
            title=notification.title,
            message=notification.message,
            url=notification.url,
            read=notification.read,
        )
        self.db.add(m)
        self.db.commit()
        self.db.refresh(m)
        return _to_entity(m)

    def patch(self, notification: Notification, data: dict) -> None:
        m = self.db.query(NotificationModel).filter(NotificationModel.id == notification.id).first()
        if m is None:
            return
        allowed = {"read", "title", "message"}
        for key, val in data.items():
            if key in allowed:
                setattr(m, key, val)
        self.db.commit()

    def find_by_id(self, nid: int) -> Optional[Notification]:
        m = self.db.query(NotificationModel).filter(NotificationModel.id == nid).first()
        return _to_entity(m) if m else None
