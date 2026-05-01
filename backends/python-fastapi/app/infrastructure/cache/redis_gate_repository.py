"""
Gate JWT Redis キャッシュリポジトリモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from typing import Optional
import redis
from app.config.settings import get_settings


class RedisGateRepository:
    """Gate JWT の Redis キャッシュリポジトリ。

    Attributes:
        rdb: Redis クライアント
        settings: アプリケーション設定
    """

    def __init__(self, rdb: redis.Redis):
        """初期化します。

        Args:
            rdb: Redis クライアント
        """
        self.rdb = rdb
        self.settings = get_settings()

    def _key(self, identifier: str, member: str) -> str:
        """Redis キャッシュキーを生成します。

        Args:
            identifier: クライアント識別子
            member: 会員ID

        Returns:
            Redis キーの文字列
        """
        prefix = self.settings.cache_prefix
        return f"{prefix}:gate:{identifier}:{member}"

    def get_jwt(self, identifier: str, member: str) -> Optional[str]:
        """キャッシュから JWT を取得します。

        Args:
            identifier: クライアント識別子
            member: 会員ID

        Returns:
            JWT 文字列、または None
        """
        return self.rdb.get(self._key(identifier, member))

    def put_jwt(self, identifier: str, member: str, token: str) -> None:
        """JWT をキャッシュに保存します。

        Args:
            identifier: クライアント識別子
            member: 会員ID
            token: JWT 文字列
        """
        ttl = self.settings.gate_jwt_cache_ttl
        self.rdb.setex(self._key(identifier, member), ttl, token)
