import redis
from app.config.settings import get_settings


def get_redis() -> redis.Redis:
    s = get_settings()
    return redis.Redis(
        host=s.redis_host,
        port=s.redis_port,
        password=s.redis_password or None,
        db=s.redis_db,
        decode_responses=True,
    )
