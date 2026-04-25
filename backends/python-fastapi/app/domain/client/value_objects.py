"""
クライアントドメイン バリューオブジェクトモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import dataclass
from datetime import datetime
from typing import Optional


@dataclass(frozen=True)
class ClientListItem:
    """クライアント一覧レスポンス用バリューオブジェクト。"""

    id: int
    name: str
    identifier: str
    status: int
    started_at: Optional[datetime]
    stopped_at: Optional[datetime]
    created_at: Optional[datetime]
    updated_at: Optional[datetime]


@dataclass(frozen=True)
class ClientDetailVo:
    """クライアント詳細レスポンス用バリューオブジェクト。"""

    id: int
    name: str
    identifier: str
    post_code: str
    pref: str
    city: str
    address: str
    building: str
    tel: str
    email: str
    status: int
    fingerprint: Optional[str]
    started_at: Optional[datetime]
    stopped_at: Optional[datetime]
    created_at: Optional[datetime]
    updated_at: Optional[datetime]


@dataclass(frozen=True)
class ClientStoreResultVo:
    """クライアント登録結果バリューオブジェクト。メール送信・通知配信に必要なフィールドを含む。"""

    id: int
    name: str
    identifier: str
    email: str
    token: str
