"""
スタッフドメイン リポジトリインターフェースモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from abc import ABC, abstractmethod
from typing import Optional
from app.domain.staff.entity import Staff
from app.domain.staff.condition import StaffCondition


class StaffRepository(ABC):
    """スタッフの永続化インターフェース。"""

    @abstractmethod
    def find_all_staffs(self, cond: StaffCondition) -> list[Staff]:
        """検索条件に合致するスタッフの一覧を返します。

        Args:
            cond: 検索条件

        Returns:
            Staff エンティティのリスト
        """
        ...

    @abstractmethod
    def find_staff_by_id(self, staff_id: int) -> Optional[Staff]:
        """IDでアクティブなスタッフを返します。存在しない場合は None を返します。

        Args:
            staff_id: スタッフID

        Returns:
            Staff エンティティ、または None
        """
        ...

    @abstractmethod
    def find_staff_by_id_include_deleted(self, staff_id: int) -> Optional[Staff]:
        """IDで論理削除済みを含むスタッフを返します。存在しない場合は None を返します。

        Args:
            staff_id: スタッフID

        Returns:
            Staff エンティティ、または None
        """
        ...

    @abstractmethod
    def find_staff_by_provider(self, provider: int, provider_id: str) -> Optional[Staff]:
        """OAuthプロバイダーとプロバイダーIDでスタッフを返します。

        Args:
            provider: プロバイダー種別
            provider_id: プロバイダーが発行するユーザーID

        Returns:
            Staff エンティティ、または None
        """
        ...

    @abstractmethod
    def find_all_active_staffs(self) -> list[Staff]:
        """全アクティブスタッフの一覧を返します。

        Returns:
            Staff エンティティのリスト
        """
        ...

    @abstractmethod
    def save_staff(self, staff: Staff) -> Staff:
        """スタッフを保存し、保存後のエンティティを返します。

        Args:
            staff: 保存するスタッフエンティティ

        Returns:
            保存後の Staff エンティティ
        """
        ...

    @abstractmethod
    def update_staff_role(self, staff: Staff, role: int) -> None:
        """スタッフの権限を更新します。

        Args:
            staff: 更新対象のスタッフエンティティ
            role: 新しい権限値
        """
        ...

    @abstractmethod
    def soft_delete_staff(self, staff: Staff) -> None:
        """スタッフを論理削除します。

        Args:
            staff: 論理削除するスタッフエンティティ
        """
        ...

    @abstractmethod
    def restore_staff(self, staff: Staff) -> None:
        """スタッフの論理削除を復元します。

        Args:
            staff: 復元するスタッフエンティティ
        """
        ...
