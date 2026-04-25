"""
認証ユースケース Dto モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import dataclass
from typing import Optional


@dataclass
class AuthLoginDto:
    """OAuth ログイン Dto。"""

    provider: int
    provider_id: str
    name: str
    email: str
    avatar: Optional[str] = None


@dataclass
class AuthMeDto:
    """ログイン中スタッフ情報 Dto。"""

    staff_id: int
    name: str
    avatar: Optional[str]
    role: int
