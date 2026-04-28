"""
認証ユースケース Interactor モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from datetime import datetime, timezone
from typing import Optional

from app.domain.invitation.auth_repository import InvitationAuthRepository
from app.domain.staff.entity import Staff
from app.domain.staff.repository import StaffRepository
from app.domain.staff.value_objects import StaffVo
from app.exceptions import forbidden
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
        invitation_auth_repo: 招待認証キャッシュリポジトリ
    """

    def __init__(self, staff_repo: StaffRepository, invitation_auth_repo: InvitationAuthRepository):
        """初期化します。

        Args:
            staff_repo: スタッフリポジトリ
            invitation_auth_repo: 招待認証キャッシュリポジトリ
        """
        self.staff_repo = staff_repo
        self.invitation_auth_repo = invitation_auth_repo

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
        """ソーシャル認証でログインし、レスポンス用 Vo を返します。未登録の場合は招待トークンを検証して新規作成します。

        Args:
            dto: ログイン情報 Dto

        Returns:
            StaffVo インスタンス
        """
        staff = self.staff_repo.find_staff_by_provider(dto.provider, dto.provider_id)
        if staff is None:
            token = dto.invitation_token
            if not token or self.invitation_auth_repo.find(token) is None:
                raise forbidden("invitation_required")
            self.invitation_auth_repo.remove(token)
            now = datetime.now(timezone.utc)
            staff = Staff(
                provider=dto.provider,
                provider_id=dto.provider_id,
                name=dto.name,
                email=dto.email,
                avatar=dto.avatar,
                role=0,
                last_login_at=now,
            )
        else:
            staff.name = dto.name
            staff.email = dto.email
            staff.avatar = dto.avatar
            staff.last_login_at = datetime.now(timezone.utc)
        saved = self.staff_repo.save_staff(staff)
        return _staff_to_vo(saved)
