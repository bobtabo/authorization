"""認証エンドポイントのテスト。"""

from tests.conftest import make_staff, make_invitation


class TestGetMyProfile:
    def test_認証済みでプロフィールが取得できる(self, client, db_session):
        staff = make_staff(db_session)
        res = client.get("/api/auth/me", cookies={"staff_id": str(staff.id)})
        assert res.status_code == 200
        data = res.json()
        assert data["staff_id"] == staff.id
        assert data["name"] == staff.name
        assert "role" in data

    def test_未認証で401が返る(self, client):
        res = client.get("/api/auth/me")
        assert res.status_code == 401


class TestLogin:
    def test_ログインURLが返る(self, client):
        res = client.get("/api/auth/login")
        assert res.status_code == 200
        assert "login_url" in res.json()


class TestLogout:
    def test_ログアウトが成功する(self, client):
        res = client.get("/api/auth/logout")
        assert res.status_code == 200
        assert res.json().get("message") == "logged_out"


class TestInvitation:
    def test_有効なトークンで招待情報が取得できる(self, client, db_session):
        inv = make_invitation(db_session, token="valid-test-token")
        res = client.get(f"/api/auth/invitation/{inv.token}")
        assert res.status_code == 200
        data = res.json()
        assert data["token"] == inv.token
