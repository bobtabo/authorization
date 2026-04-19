from dataclasses import dataclass
from typing import Optional


@dataclass
class AuthLoginDto:
    provider: int
    provider_id: str
    name: str
    email: str
    avatar: Optional[str] = None


@dataclass
class AuthMeDto:
    staff_id: int
    name: str
    avatar: Optional[str]
    role: int
