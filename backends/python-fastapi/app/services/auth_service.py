from typing import Optional
from app.models.models import Staff
from app.repositories.staff_repo import StaffRepository


class AuthService:
    def __init__(self, staff_repo: StaffRepository):
        self.staff_repo = staff_repo

    def find_user(self, staff_id: int) -> Optional[Staff]:
        return self.staff_repo.find_by_id(staff_id)

    def login(self, provider: int, provider_id: str, name: str, email: str, avatar: Optional[str]) -> Staff:
        staff = self.staff_repo.find_by_provider(provider, provider_id)
        if staff is None:
            staff = Staff(
                provider=provider,
                provider_id=provider_id,
                name=name,
                email=email,
                avatar=avatar,
                role=0,
            )
        else:
            staff.name = name
            staff.email = email
            staff.avatar = avatar
        return self.staff_repo.save(staff)
