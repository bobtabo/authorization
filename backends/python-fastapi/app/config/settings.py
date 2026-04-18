from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict
import os


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=os.getenv("ENV_FILE", ".env"), env_file_encoding="utf-8", extra="ignore")

    app_env: str = "local"
    app_port: int = 8000
    frontend_url: str = "http://localhost:3000"
    staff_cookie_lifetime: int = 60          # 分
    notification_default_limit: int = 10
    cache_prefix: str = ""

    db_host: str = "localhost"
    db_port: int = 3306
    db_database: str = "authorization"
    db_username: str = "root"
    db_password: str = ""

    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: str = ""
    redis_db: int = 0

    google_client_id: str = ""
    google_client_secret: str = ""
    google_redirect_url: str = ""

    gate_jwt_cache_ttl: int = 1800

    mail_host: str = "localhost"
    mail_port: int = 1025
    mail_username: str = ""
    mail_password: str = ""
    mail_from_address: str = "no-reply@example.com"
    app_name: str = "Authorization Gateway"

    # JWT固定値
    jwt_issuer: str = "authorization"
    jwt_algorithm: str = "RS256"
    jwt_ttl: int = 1800

    @property
    def db_url(self) -> str:
        return (
            f"mysql+pymysql://{self.db_username}:{self.db_password}"
            f"@{self.db_host}:{self.db_port}/{self.db_database}"
            f"?charset=utf8mb4"
        )


@lru_cache
def get_settings() -> Settings:
    return Settings()
