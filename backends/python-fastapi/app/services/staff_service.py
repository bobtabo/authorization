from app.exceptions import not_found, bad_request
from app.models.models import Staff
from app.repositories.staff_repo import StaffRepository


def staff_status(staff: Staff) -> int:
    return 0 if staff.deleted_at is not None else 1


class StaffService:
    def __init__(self, staff_repo: StaffRepository):
        self.repo = staff_repo

    def find_by_condition(self, keyword=None, roles=None) -> list[Staff]:
        return self.repo.find_all(keyword=keyword, roles=roles)

    def update_role(self, staff_id: int, role: int, executor_id: int) -> None:
        if staff_id == executor_id:
            raise bad_request("cannot_update_own_role")
        staff = self.repo.find_by_id(staff_id)
        if staff is None:
            raise not_found("staff_not_found")
        self.repo.update_role(staff, role)

    def restore(self, staff_id: int) -> None:
        # soft-deleted も含めて検索
        staff = self.repo.db.query(Staff).filter(Staff.id == staff_id).first()
        if staff is None:
            raise not_found("staff_not_found")
        self.repo.restore(staff)

    def destroy(self, staff_id: int, executor_id: int) -> None:
        if staff_id == executor_id:
            raise bad_request("cannot_delete_self")
        staff = self.repo.find_by_id(staff_id)
        if staff is None:
            raise not_found("staff_not_found")
        self.repo.soft_delete(staff)
