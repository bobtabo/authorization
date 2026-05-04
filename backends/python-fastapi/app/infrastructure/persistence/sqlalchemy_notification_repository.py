"""
通知リポジトリ SQLAlchemy 実装モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
import base64
from datetime import datetime, timezone
from typing import Optional

from sqlalchemy.orm import Session

from app.domain.notification.entity import Notification
from app.domain.notification.repository import NotificationRepository
from app.exceptions import conflict
from app.infrastructure.model.model import NotificationModel
from app.support.assign import assign


def _encode_cursor(ts: int, nid: int) -> str:
    """カーソルを base64(unix_timestamp,id) 形式にエンコードします。

    Args:
        ts: Unix タイムスタンプ
        nid: 通知ID

    Returns:
        Base64 エンコードされたカーソル文字列
    """
    raw = f"{ts},{nid}"
    return base64.b64encode(raw.encode()).decode()


def _decode_cursor(cursor: str) -> tuple[int, int]:
    """カーソルをデコードして (unix_timestamp, id) を返します。

    Args:
        cursor: Base64 エンコードされたカーソル文字列

    Returns:
        (unix_timestamp, id) のタプル
    """
    raw = base64.b64decode(cursor.encode()).decode()
    ts_str, id_str = raw.split(",", 1)
    return int(ts_str), int(id_str)


def _to_entity(m: NotificationModel) -> Notification:
    return assign(Notification(), m)


class SqlAlchemyNotificationRepository(NotificationRepository):
    """NotificationRepository の SQLAlchemy 実装。

    Attributes:
        db: SQLAlchemy セッション
    """

    def __init__(self, db: Session):
        """初期化します。

        Args:
            db: SQLAlchemy セッション
        """
        self.db = db

    def list_page(
        self, staff_id: int, cursor: Optional[str], limit: int
    ) -> tuple[list[Notification], Optional[str]]:
        """カーソルページングで通知エンティティ一覧と次カーソルを返します。

        Args:
            staff_id: スタッフID
            cursor: ページカーソル（None で先頭から）
            limit: 取得件数上限

        Returns:
            (通知エンティティのリスト, 次ページカーソル) のタプル
        """
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

        return [_to_entity(m) for m in rows], next_cursor

    def counts(self, staff_id: int) -> tuple[int, int]:
        """未読数と全件数を返します。

        Args:
            staff_id: スタッフID

        Returns:
            (未読数, 全件数) のタプル
        """
        total = self.db.query(NotificationModel).filter(
            NotificationModel.staff_id == staff_id
        ).count()
        unread = self.db.query(NotificationModel).filter(
            NotificationModel.staff_id == staff_id,
            NotificationModel.read == False,  # noqa: E712
        ).count()
        return unread, total

    def bulk_mark_read(self, executor_id: int, ids: list[int], all_flag: bool) -> int:
        """条件に一致する通知を既読にして更新件数を返します。

        Args:
            executor_id: 操作者スタッフID
            ids: 対象通知IDリスト
            all_flag: True の場合は全通知を既読化

        Returns:
            更新件数
        """
        q = self.db.query(NotificationModel).filter(
            NotificationModel.staff_id == executor_id
        )
        if not all_flag:
            q = q.filter(NotificationModel.id.in_(ids))
        count = q.filter(NotificationModel.read == False).count()  # noqa: E712
        q.filter(NotificationModel.read == False).update(  # noqa: E712
            {"read": True}, synchronize_session=False
        )
        self.db.flush()
        return count

    def store(self, notification: Notification) -> Notification:
        """新規通知を保存して返します。

        Args:
            notification: 保存する通知エンティティ

        Returns:
            保存済み通知エンティティ
        """
        m = NotificationModel(
            staff_id=notification.staff_id,
            message_type=notification.message_type,
            title=notification.title,
            message=notification.message,
            url=notification.url,
            read=notification.read,
            created_by=notification.created_by,
            updated_by=notification.updated_by,
        )
        self.db.add(m)
        self.db.flush()
        self.db.refresh(m)
        return _to_entity(m)

    def patch(self, notification: Notification, data: dict) -> None:
        """通知を部分更新します。

        Args:
            notification: 更新対象の通知エンティティ
            data: 更新フィールドの辞書（read / title / message）
        """
        m = self.db.query(NotificationModel).filter(
            NotificationModel.id == notification.id
        ).first()
        if m is None:
            return
        if m.version != notification.version:
            raise conflict("optimistic_lock")
        m.version += 1
        allowed = {"read", "title", "message"}
        for key, val in data.items():
            if key in allowed:
                setattr(m, key, val)
        self.db.flush()

    def find_by_id(self, nid: int) -> Optional[Notification]:
        """IDで通知エンティティを返します。存在しない場合は None を返します。

        Args:
            nid: 通知ID

        Returns:
            通知エンティティ、または None
        """
        m = self.db.query(NotificationModel).filter(NotificationModel.id == nid).first()
        return _to_entity(m) if m else None
