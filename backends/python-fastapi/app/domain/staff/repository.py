from abc import ABC, abstractmethod
from typing import Optional
from app.domain.staff.entity import Staff
from app.domain.staff.condition import StaffCondition


class StaffRepository(ABC):
    """スタッフの永続化インターフェース。"""

    @abstractmethod
    def find_all_staffs(self, cond: StaffCondition) -> list[Staff]:
        ...

    @abstractmethod
    def find_staff_by_id(self, staff_id: int) -> Optional[Staff]:
        ...

    @abstractmethod
    def find_staff_by_id_include_deleted(self, staff_id: int) -> Optional[Staff]:
        ...

    @abstractmethod
    def find_staff_by_provider(self, provider: int, provider_id: str) -> Optional[Staff]:
        ...

    @abstractmethod
    def find_all_active_staffs(self) -> list[Staff]:
        ...

    @abstractmethod
    def save_staff(self, staff: Staff) -> Staff:
        ...

    @abstractmethod
    def update_staff_role(self, staff: Staff, role: int) -> None:
        ...

    @abstractmethod
    def soft_delete_staff(self, staff: Staff) -> None:
        ...

    @abstractmethod
    def restore_staff(self, staff: Staff) -> None:
        ...
