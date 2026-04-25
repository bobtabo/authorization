"""
認証ユースケース Interactor モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from typing import Optional

from app.domain.staff.entity import Staff
from app.domain.staff.repository import StaffRepository
from app.domain.staff.value_objects import StaffVo
from app.usecase.auth.dto import AuthLoginDto


def _staff_to_vo(staff: Staff) -> StaffVo:
    """スタッフエンティティをレスポンス用 Vo に変換します。

    Args:
        staff: スタッフエンティティ

    Returns:
        StaffVo インスタンス
    """
    return StaffVo(id=staff.id, name=staff.name, avatar=staff.avatar, role=staff.role)


class AuthInteractor:
    """認証のユースケース実装。

    Attributes:
        staff_repo: スタッフリポジトリ
    """

    def __init__(self, staff_repo: StaffRepository):
        """初期化します。

        Args:
            staff_repo: スタッフリポジトリ
        """
        self.staff_repo = staff_repo

    def find_user(self, staff_id: int) -> Optional[StaffVo]:
        """IDでスタッフを取得し、レスポンス用 Vo を返します。

        Args:
            staff_id: スタッフID

        Returns:
            StaffVo、または None
        """
        staff = self.staff_repo.find_staff_by_id(staff_id)
        if staff is None:
            return None
        return _staff_to_vo(staff)

    def login(self, dto: AuthLoginDto) -> StaffVo:
        """ソーシャル認証でログインし、レスポンス用 Vo を返します。未登録の場合は新規スタッフを作成します。

        Args:
            dto: ログイン情報 Dto

        Returns:
            StaffVo インスタンス
        """
        staff = self.staff_repo.find_staff_by_provider(dto.provider, dto.provider_id)
        if staff is None:
            staff = Staff(
                provider=dto.provider,
                provider_id=dto.provider_id,
                name=dto.name,
                email=dto.email,
                avatar=dto.avatar,
                role=0,
            )
        else:
            staff.name = dto.name
            staff.email = dto.email
            staff.avatar = dto.avatar
        saved = self.staff_repo.save_staff(staff)
        return _staff_to_vo(saved)
