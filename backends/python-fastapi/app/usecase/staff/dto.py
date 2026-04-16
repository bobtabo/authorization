from dataclasses import dataclass
from typing import Optional


@dataclass
class StaffLoginDto:
    provider: int
    provider_id: str
    name: str
    email: str
    avatar: Optional[str] = None


@dataclass
class StaffUpdateRoleDto:
    staff_id: int
    role: int
    executor_id: int


@dataclass
class StaffDestroyDto:
    staff_id: int
    executor_id: int
