"""
スタッフユースケース Interactor モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from typing import Optional

from app.domain.staff.entity import Staff
from app.domain.staff.condition import StaffCondition
from app.domain.staff.repository import StaffRepository
from app.domain.staff.value_objects import StaffListItem
from app.exceptions import not_found, bad_request
from app.usecase.staff.dto import StaffUpdateRoleDto, StaffDestroyDto


def _to_list_item(staff: Staff) -> StaffListItem:
    """スタッフエンティティを一覧用 Vo に変換します。

    Args:
        staff: スタッフエンティティ

    Returns:
        StaffListItem インスタンス
    """
    status = 0 if staff.deleted_at is not None else 1
    return StaffListItem(
        id=staff.id,
        name=staff.name,
        email=staff.email,
        role=staff.role,
        status=status,
        created_at=staff.created_at,
        updated_at=staff.updated_at,
    )


class StaffInteractor:
    """スタッフのユースケース実装。

    Attributes:
        repository: スタッフリポジトリ
    """

    def __init__(self, repo: StaffRepository):
        """初期化します。

        Args:
            repo: スタッフリポジトリ
        """
        self.repository = repo

    def find_by_condition(
        self,
        keyword: Optional[str] = None,
        roles: Optional[list[int]] = None,
        offset: int = 0,
        limit: int = 10,
        sort: Optional[str] = None,
        sort_type: Optional[str] = None,
    ) -> tuple[list[StaffListItem], int]:
        """検索条件に合致するスタッフ一覧の Vo と総件数を返します。

        Args:
            keyword: キーワード検索文字列
            roles: ロールフィルター
            offset: オフセット
            limit: 取得件数
            sort: ソート対象
            sort_type: ソート順

        Returns:
            StaffListItem のリストと総件数のタプル
        """
        cond = StaffCondition(keyword=keyword, roles=roles or [], offset=offset, limit=limit, sort=sort, sort_type=sort_type)
        count = self.repository.count_staffs(cond)
        staffs = self.repository.find_all_staffs(cond)
        return [_to_list_item(s) for s in staffs], count

    def update_role(self, dto: StaffUpdateRoleDto) -> None:
        """スタッフの権限を更新します。

        Args:
            dto: ロール更新 Dto

        Raises:
            AppException: 自分自身のロール更新、またはスタッフが存在しない場合
        """
        if dto.staff_id == dto.executor_id:
            raise bad_request("cannot_update_own_role")
        staff = self.repository.find_staff_by_id(dto.staff_id)
        if staff is None:
            raise not_found("staff_not_found")
        self.repository.update_staff_role(staff, dto.role)

    def restore(self, staff_id: int) -> None:
        """スタッフの論理削除を復元します。

        Args:
            staff_id: スタッフID

        Raises:
            AppException: スタッフが存在しない場合
        """
        staff = self.repository.find_staff_by_id_include_deleted(staff_id)
        if staff is None:
            raise not_found("staff_not_found")
        self.repository.restore_staff(staff)

    def destroy(self, dto: StaffDestroyDto) -> None:
        """スタッフを論理削除します。

        Args:
            dto: 論理削除 Dto

        Raises:
            AppException: 自分自身の削除、またはスタッフが存在しない場合
        """
        if dto.staff_id == dto.executor_id:
            raise bad_request("cannot_delete_self")
        staff = self.repository.find_staff_by_id(dto.staff_id)
        if staff is None:
            raise not_found("staff_not_found")
        self.repository.soft_delete_staff(staff)
