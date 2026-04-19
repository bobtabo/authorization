from dataclasses import dataclass
from typing import Optional


@dataclass(frozen=True)
class StaffMutationVo:
    """スタッフ作成・更新用バリューオブジェクト。"""
    provider: int
    provider_id: str
    name: str
    email: str
    avatar: Optional[str] = None
    role: int = 0
