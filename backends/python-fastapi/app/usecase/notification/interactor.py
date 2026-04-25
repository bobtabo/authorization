"""
通知ユースケース Interactor モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from typing import Optional

from app.domain.notification.entity import Notification
from app.domain.notification.value_objects import NotificationItem, NotificationPage
from app.domain.notification.repository import NotificationRepository
from app.domain.staff.repository import StaffRepository
from app.exceptions import not_found
from app.usecase.notification.dto import NotificationStoreDto


def _to_item(n: Notification) -> NotificationItem:
    """通知エンティティをレスポンス用 Vo に変換します。

    Args:
        n: 通知エンティティ

    Returns:
        NotificationItem インスタンス
    """
    return NotificationItem(
        id=n.id,
        staff_id=n.staff_id,
        message_type=n.message_type,
        title=n.title,
        message=n.message,
        url=n.url,
        read=n.read,
        created_at=n.created_at.strftime("%Y-%m-%d %H:%M") if n.created_at else "",
        updated_at=n.updated_at.strftime("%Y-%m-%d %H:%M") if n.updated_at else "",
    )


class NotificationInteractor:
    """通知のユースケース実装。

    Attributes:
        notif_repo: 通知リポジトリ
        staff_repo: スタッフリポジトリ
    """

    def __init__(self, notif_repo: NotificationRepository, staff_repo: StaffRepository):
        """初期化します。

        Args:
            notif_repo: 通知リポジトリ
            staff_repo: スタッフリポジトリ（FanOut に使用）
        """
        self.notif_repo = notif_repo
        self.staff_repo = staff_repo

    def list_page(self, staff_id: int, cursor: Optional[str], limit: int) -> NotificationPage:
        """カーソルページングで通知一覧の Vo を返します。

        Args:
            staff_id: スタッフID
            cursor: ページカーソル（None で先頭から）
            limit: 取得件数上限（1〜100 にクランプ）

        Returns:
            NotificationPage インスタンス
        """
        if limit < 1:
            limit = 1
        if limit > 100:
            limit = 100
        notifications, next_cursor = self.notif_repo.list_page(staff_id, cursor, limit)
        items = [_to_item(n) for n in notifications]
        return NotificationPage(items=items, next_cursor=next_cursor)

    def counts(self, staff_id: int) -> tuple[int, int]:
        """スタッフの未読・全体通知数を返します。

        Args:
            staff_id: スタッフID

        Returns:
            (未読数, 全件数) のタプル
        """
        return self.notif_repo.counts(staff_id)

    def bulk_mark_read(self, staff_id: int) -> int:
        """スタッフの全通知を既読にして更新件数を返します。

        Args:
            staff_id: スタッフID

        Returns:
            更新件数
        """
        return self.notif_repo.bulk_mark_read(staff_id, [], True)

    def mark_read(self, notification_id: int) -> None:
        """通知を既読にします。

        Args:
            notification_id: 通知ID

        Raises:
            AppException: 通知が存在しない場合
        """
        notif = self.notif_repo.find_by_id(notification_id)
        if notif is None:
            raise not_found("notification_not_found")
        self.notif_repo.patch(notif, {"read": True})

    def fan_out(
        self,
        title: str,
        body: Optional[str] = None,
        url: Optional[str] = None,
        executor_id: int = 0,
        message_type: int = 1,
    ) -> None:
        """全アクティブスタッフへ通知を配信します。

        Args:
            title: 通知タイトル
            body: 通知本文
            url: 通知リンク URL
            executor_id: 操作者スタッフID
            message_type: メッセージ種別
        """
        staffs = self.staff_repo.find_all_active_staffs()
        for staff in staffs:
            n = Notification(
                staff_id=staff.id,
                message_type=message_type,
                title=title,
                message=body or "",
                url=url,
                read=False,
                created_by=executor_id,
                updated_by=executor_id,
            )
            self.notif_repo.store(n)
