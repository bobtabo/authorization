from dataclasses import dataclass
from typing import Optional


@dataclass
class ClientStoreDto:
    name: str
    post_code: str = ""
    pref: str = ""
    city: str = ""
    address: str = ""
    building: str = ""
    tel: str = ""
    email: str = ""
    executor_id: int = 0


@dataclass
class ClientUpdateDto:
    client_id: int
    name: Optional[str] = None
    post_code: Optional[str] = None
    pref: Optional[str] = None
    city: Optional[str] = None
    address: Optional[str] = None
    building: Optional[str] = None
    tel: Optional[str] = None
    email: Optional[str] = None
    status: Optional[int] = None
