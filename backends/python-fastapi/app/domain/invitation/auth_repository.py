"""
招待認証リポジトリインターフェースモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from abc import ABC, abstractmethod
from typing import Optional


class InvitationAuthRepository(ABC):
    """招待認証トークンのキャッシュリポジトリインターフェース。"""

    @abstractmethod
    def store(self, token: str, ttl: int) -> None:
        """トークンを指定秒数キャッシュします。"""

    @abstractmethod
    def find(self, token: str) -> Optional[str]:
        """トークンを取得します。存在しない場合は None を返します。"""

    @abstractmethod
    def remove(self, token: str) -> None:
        """トークンを削除します。"""
