from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class GateIssueVo:
    """Gate JWT 発行バリューオブジェクト。"""
    token: str


@dataclass(frozen=True)
class GateVerifyVo:
    """Gate JWT 検証バリューオブジェクト。"""
    identifier: str
    member: str
    fingerprint: str
    payload: dict
