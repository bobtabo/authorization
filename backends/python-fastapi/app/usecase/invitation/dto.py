"""
招待ユースケース Dto モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import dataclass


@dataclass
class InvitationResultDto:
    """招待結果 Dto。"""

    token: str
    url: str
    display_url: str
