"""
招待ドメイン バリューオブジェクトモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import dataclass


@dataclass(frozen=True)
class InvitationVo:
    """招待URLバリューオブジェクト。"""
    token: str
    url: str
    display_url: str
