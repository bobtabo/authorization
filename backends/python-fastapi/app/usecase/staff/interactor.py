from typing import Optional

from app.domain.staff.entity import Staff
from app.domain.staff.condition import StaffCondition
from app.domain.staff.repository import StaffRepository
from app.exceptions import not_found, bad_request
from app.usecase.staff.dto import StaffUpdateRoleDto, StaffDestroyDto


def staff_status(staff: Staff) -> int:
    return 0 if staff.deleted_at is not None else 1


class StaffInteractor:
    """スタッフのユースケース実装。"""

    def __init__(self, repo: StaffRepository):
        self.repo = repo

    def find_by_condition(self, keyword: Optional[str] = None, roles: Optional[list[int]] = None) -> list[Staff]:
        cond = StaffCondition(keyword=keyword, roles=roles or [])
        return self.repo.find_all_staffs(cond)

    def update_role(self, dto: StaffUpdateRoleDto) -> None:
        if dto.staff_id == dto.executor_id:
            raise bad_request("cannot_update_own_role")
        staff = self.repo.find_staff_by_id(dto.staff_id)
        if staff is None:
            raise not_found("staff_not_found")
        self.repo.update_staff_role(staff, dto.role)

    def restore(self, staff_id: int) -> None:
        staff = self.repo.find_staff_by_id_include_deleted(staff_id)
        if staff is None:
            raise not_found("staff_not_found")
        self.repo.restore_staff(staff)

    def destroy(self, dto: StaffDestroyDto) -> None:
        if dto.staff_id == dto.executor_id:
            raise bad_request("cannot_delete_self")
        staff = self.repo.find_staff_by_id(dto.staff_id)
        if staff is None:
            raise not_found("staff_not_found")
        self.repo.soft_delete_staff(staff)
