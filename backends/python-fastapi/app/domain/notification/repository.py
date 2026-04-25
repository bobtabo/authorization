"""
通知ドメイン リポジトリインターフェースモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from abc import ABC, abstractmethod
from typing import Optional

from app.domain.notification.entity import Notification


class NotificationRepository(ABC):
    """通知の永続化インターフェース。"""

    @abstractmethod
    def list_page(
        self, staff_id: int, cursor: Optional[str], limit: int
    ) -> tuple[list[Notification], Optional[str]]:
        """カーソルページングで通知エンティティ一覧と次カーソルを返します。

        Args:
            staff_id: スタッフID
            cursor: ページカーソル（None で先頭から）
            limit: 取得件数上限

        Returns:
            通知エンティティのリストと次ページカーソルのタプル
        """
        ...

    @abstractmethod
    def counts(self, staff_id: int) -> tuple[int, int]:
        """未読数と全件数を返します。

        Args:
            staff_id: スタッフID

        Returns:
            (未読数, 全件数) のタプル
        """
        ...

    @abstractmethod
    def bulk_mark_read(self, executor_id: int, ids: list[int], all_flag: bool) -> int:
        """条件に一致する通知を既読にして更新件数を返します。

        Args:
            executor_id: 操作者スタッフID
            ids: 対象通知IDリスト（all_flag=True の場合は無視）
            all_flag: True の場合は全通知を既読化

        Returns:
            更新件数
        """
        ...

    @abstractmethod
    def store(self, notification: Notification) -> Notification:
        """新規通知を保存して返します。

        Args:
            notification: 通知エンティティ

        Returns:
            保存済み通知エンティティ
        """
        ...

    @abstractmethod
    def patch(self, notification: Notification, data: dict) -> None:
        """通知を部分更新します。

        Args:
            notification: 更新対象の通知エンティティ
            data: 更新フィールドの辞書（read / title / message）
        """
        ...

    @abstractmethod
    def find_by_id(self, nid: int) -> Optional[Notification]:
        """IDで通知エンティティを返します。存在しない場合は None を返します。

        Args:
            nid: 通知ID

        Returns:
            通知エンティティ、または None
        """
        ...
