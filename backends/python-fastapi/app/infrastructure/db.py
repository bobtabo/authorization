"""
データベース接続・セッション管理モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session

from app.config.settings import get_settings


def get_engine():
    """SQLAlchemy エンジンを生成します。

    Returns:
        SQLAlchemy エンジンインスタンス
    """
    settings = get_settings()
    return create_engine(settings.db_url, pool_pre_ping=True)


_engine = get_engine()
SessionLocal = sessionmaker(bind=_engine, autocommit=False, autoflush=False)


def get_db() -> Session:
    """FastAPI 依存性注入用のデータベースセッションを提供します。

    Unit of Work パターンを採用し、エンドポイント処理完了後に自動コミット、
    例外発生時は自動ロールバックします。

    Yields:
        SQLAlchemy セッションインスタンス
    """
    db = SessionLocal()
    try:
        yield db
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()
