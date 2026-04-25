"""
Gate ユースケース Dto モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import dataclass


@dataclass
class GateIssueDto:
    """Gate JWT 発行 Dto。"""

    access_token: str
    member: str


@dataclass
class GateVerifyDto:
    """Gate JWT 検証 Dto。"""

    identifier: str
    token: str
