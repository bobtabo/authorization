"""
招待認証 Redis キャッシュリポジトリモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from typing import Optional
import redis
from app.config.settings import get_settings
from app.domain.invitation.auth_repository import InvitationAuthRepository


class RedisInvitationAuthRepository(InvitationAuthRepository):
    """招待認証トークンの Redis キャッシュリポジトリ。"""

    def __init__(self, rdb: redis.Redis):
        self.rdb = rdb
        self.settings = get_settings()

    def _key(self, token: str) -> str:
        prefix = self.settings.cache_prefix
        return f"{prefix}:invitation_auth:invitation_auth:{token}"

    def store(self, token: str, ttl: int) -> None:
        self.rdb.setex(self._key(token), ttl, token)

    def find(self, token: str) -> Optional[str]:
        val = self.rdb.get(self._key(token))
        return val.decode() if isinstance(val, bytes) else val

    def remove(self, token: str) -> None:
        self.rdb.delete(self._key(token))
