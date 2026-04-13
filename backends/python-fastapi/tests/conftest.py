"""
pytest 共通設定・フィクスチャ。

authorization_test DB（MySQL）を使い、各テスト前にテーブルをクリアします。
"""

import secrets

import pytest
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, text
from sqlalchemy.engine import URL
from sqlalchemy.orm import sessionmaker

from app.infrastructure.db import get_db
from app.main import app
from app.models.models import Base, Client, Invitation, Notification, Staff
from app.routers.deps import get_redis_client

# ---------------------------------------------------------------------------
# テスト用 DB エンジン
# ---------------------------------------------------------------------------
_db_url = URL.create(
    "mysql+pymysql",
    username="develop",
    password="docker#DOCKER1234",
    host="host.docker.internal",
    port=3306,
    database="authorization_test",
    query={"charset": "utf8mb4"},
)
test_engine = create_engine(_db_url, echo=False)
TestingSession = sessionmaker(bind=test_engine, autocommit=False, autoflush=False)

# テーブルをモデル定義に基づいて作成（存在しない場合のみ）
Base.metadata.create_all(test_engine)


# ---------------------------------------------------------------------------
# テスト用フェイク Redis（実 Redis への依存を除去）
# ---------------------------------------------------------------------------
class _FakeRedis:
    def __init__(self):
        self._store: dict = {}

    def get(self, key: str):
        return self._store.get(key)

    def setex(self, key: str, ttl: int, value: str) -> None:
        self._store[key] = value

    def delete(self, key: str) -> None:
        self._store.pop(key, None)


_fake_redis = _FakeRedis()


def override_get_redis():
    return _fake_redis


# ---------------------------------------------------------------------------
# get_db 依存性のオーバーライド
# ---------------------------------------------------------------------------
def override_get_db():
    db = TestingSession()
    try:
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = override_get_db
app.dependency_overrides[get_redis_client] = override_get_redis


# ---------------------------------------------------------------------------
# フィクスチャ
# ---------------------------------------------------------------------------
@pytest.fixture(autouse=True)
def clean_db():
    """テストごとに全テーブルとフェイク Redis をクリアする。"""
    db = TestingSession()
    db.execute(text("SET FOREIGN_KEY_CHECKS=0"))
    for table in reversed(Base.metadata.sorted_tables):
        db.execute(text(f"TRUNCATE TABLE `{table.name}`"))
    db.execute(text("SET FOREIGN_KEY_CHECKS=1"))
    db.commit()
    db.close()
    _fake_redis._store.clear()
    yield


@pytest.fixture
def client() -> TestClient:
    return TestClient(app, raise_server_exceptions=True)


@pytest.fixture
def db_session():
    db = TestingSession()
    try:
        yield db
    finally:
        db.close()


# ---------------------------------------------------------------------------
# テストデータ生成ヘルパー
# ---------------------------------------------------------------------------
def make_staff(db, **kwargs) -> Staff:
    defaults = {
        "name": "テストスタッフ",
        "email": "staff@example.com",
        "provider": 1,
        "provider_id": "123456789",
        "role": 1,
    }
    defaults.update(kwargs)
    staff = Staff(**defaults)
    db.add(staff)
    db.commit()
    db.refresh(staff)
    return staff


def make_client_record(db, **kwargs) -> Client:
    """RSA キーペア付きのクライアントレコードを作成します（2048bit: テスト用）。"""
    private_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    pub_pem = private_key.public_key().public_bytes(
        serialization.Encoding.PEM,
        serialization.PublicFormat.SubjectPublicKeyInfo,
    ).decode()
    priv_pem = private_key.private_bytes(
        serialization.Encoding.PEM,
        serialization.PrivateFormat.TraditionalOpenSSL,
        serialization.NoEncryption(),
    ).decode()

    defaults = {
        "name": "テストクライアント",
        "identifier": "test-client-001",
        "post_code": "100-0001",
        "pref": "東京都",
        "city": "千代田区",
        "address": "千代田1-1",
        "building": None,
        "tel": "0312345678",
        "email": "client@example.com",
        "token": secrets.token_hex(32),
        "public_key": pub_pem,
        "private_key": priv_pem,
        "fingerprint": f"SHA256:{secrets.token_hex(16)}",
        "status": 1,
    }
    defaults.update(kwargs)
    c = Client(**defaults)
    db.add(c)
    db.commit()
    db.refresh(c)
    return c


def make_invitation(db, **kwargs) -> Invitation:
    defaults = {"token": secrets.token_hex(16)}
    defaults.update(kwargs)
    inv = Invitation(**defaults)
    db.add(inv)
    db.commit()
    db.refresh(inv)
    return inv


def make_notification(db, staff_id: int, **kwargs) -> Notification:
    defaults = {
        "staff_id": staff_id,
        "title": "テスト通知",
        "body": "通知本文",
        "read": False,
    }
    defaults.update(kwargs)
    n = Notification(**defaults)
    db.add(n)
    db.commit()
    db.refresh(n)
    return n
