from typing import Optional

from app.domain.staff.entity import Staff
from app.domain.staff.repository import StaffRepository
from app.usecase.auth.dto import AuthLoginDto


class AuthInteractor:
    """認証のユースケース実装。"""

    def __init__(self, staff_repo: StaffRepository):
        self.staff_repo = staff_repo

    def find_user(self, staff_id: int) -> Optional[Staff]:
        return self.staff_repo.find_staff_by_id(staff_id)

    def login(self, dto: AuthLoginDto) -> Staff:
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
        return self.staff_repo.save_staff(staff)
