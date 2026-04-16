from typing import Optional
import redis
from app.config.settings import get_settings


class GateCacheRepository:
    """Gate JWT の Redis キャッシュリポジトリ。"""

    def __init__(self, rdb: redis.Redis):
        self.rdb = rdb
        self.settings = get_settings()

    def _key(self, identifier: str, member: str) -> str:
        prefix = self.settings.cache_prefix
        return f"{prefix}:gate:{identifier}:{member}"

    def get_jwt(self, identifier: str, member: str) -> Optional[str]:
        return self.rdb.get(self._key(identifier, member))

    def put_jwt(self, identifier: str, member: str, token: str) -> None:
        ttl = self.settings.gate_jwt_cache_ttl
        self.rdb.setex(self._key(identifier, member), ttl, token)
