from dataclasses import dataclass
from typing import Optional


@dataclass(frozen=True)
class ClientStoreVo:
    """クライアント登録用バリューオブジェクト。"""
    name: str
    identifier: str
    post_code: str = ""
    pref: str = ""
    city: str = ""
    address: str = ""
    building: str = ""
    tel: str = ""
    email: str = ""


@dataclass(frozen=True)
class ClientUpdateVo:
    """クライアント更新用バリューオブジェクト。"""
    name: Optional[str] = None
    post_code: Optional[str] = None
    pref: Optional[str] = None
    city: Optional[str] = None
    address: Optional[str] = None
    building: Optional[str] = None
    tel: Optional[str] = None
    email: Optional[str] = None
    status: Optional[int] = None
