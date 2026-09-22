"""認証エンドポイントのテスト。"""

from app.config.settings import get_settings
from app.support.staff_session import sign_staff_id
from tests.conftest import make_staff, make_invitation, sign_staff_cookie


class TestGetMyProfile:
    def test_認証済みでプロフィールが取得できる(self, client, db_session):
        staff = make_staff(db_session)
        res = client.get("/api/auth/me", cookies={"staff_id": sign_staff_cookie(staff.id)})
        assert res.status_code == 200
        data = res.json()
        assert data["staff_id"] == staff.id
        assert data["name"] == staff.name
        assert "role" in data

    def test_未認証で401が返る(self, client):
        res = client.get("/api/auth/me")
        assert res.status_code == 401

    def test_署名の無い偽造クッキーでは401が返る(self, client, db_session):
        staff = make_staff(db_session, email="forge-1@example.com")
        # 署名を付けず staff_id をそのまま設定した「偽造」クッキー。
        res = client.get("/api/auth/me", cookies={"staff_id": str(staff.id)})
        assert res.status_code == 401

    def test_署名が不正なクッキーでは401が返る(self, client, db_session):
        staff = make_staff(db_session, email="forge-2@example.com")
        signed = sign_staff_cookie(staff.id)
        # 末尾の1文字を必ず異なる値に置き換える（元の値と偶然一致すると署名が
        # 変わらずテストが不安定になるため）。
        replacement = "1" if signed[-1] == "0" else "0"
        tampered = signed[:-1] + replacement
        res = client.get("/api/auth/me", cookies={"staff_id": tampered})
        assert res.status_code == 401

    def test_別のシークレットで署名されたクッキーでは401が返る(self, client, db_session):
        staff = make_staff(db_session, email="forge-3@example.com")
        forged = sign_staff_id(staff.id, "attacker-controlled-secret", 3600)
        res = client.get("/api/auth/me", cookies={"staff_id": forged})
        assert res.status_code == 401

    def test_有効期限切れの署名済みクッキーでは401が返る(self, client, db_session):
        staff = make_staff(db_session, email="forge-4@example.com")
        # 署名自体は正しいが、Max-Ageが切れた後に手動でCookieヘッダーを
        # 再送した状況を再現する（署名対象に有効期限を含めていないと防げない）。
        expired = sign_staff_id(staff.id, get_settings().staff_cookie_secret, -3600)
        res = client.get("/api/auth/me", cookies={"staff_id": expired})
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


class TestOAuthState:
    def test_認可開始時にnonceクッキーを発行しstateに含める(self, client):
        res = client.get("/auth/google/redirect?token=inv-token", follow_redirects=False)
        assert res.status_code == 302
        nonce = res.cookies.get("oauth_state")
        assert nonce
        assert "httponly" in res.headers["set-cookie"].lower()
        assert f"state=python%7C{nonce}%7Cinv-token" in res.headers["location"]

    def test_nonceクッキーが無い場合は400エラーページへリダイレクトする(self, client):
        res = client.get("/auth/google/callback?code=x&state=python%7Cabc", follow_redirects=False)
        assert res.status_code == 302
        assert res.headers["location"].endswith("/error?code=400")

    def test_nonceが一致しない場合は400エラーページへリダイレクトする(self, client):
        res = client.get(
            "/auth/github/callback?code=x&state=python%7Cabc",
            cookies={"oauth_state": "xyz"}, follow_redirects=False,
        )
        assert res.status_code == 302
        assert res.headers["location"].endswith("/error?code=400")

    def test_stateにnonceセグメントが無い場合は400エラーページへリダイレクトする(self, client):
        res = client.get(
            "/auth/github/callback?code=x&state=python",
            cookies={"oauth_state": "abc"}, follow_redirects=False,
        )
        assert res.status_code == 302
        assert res.headers["location"].endswith("/error?code=400")

    def test_非ASCIIのnonceでも500にならず400エラーページへリダイレクトする(self, client):
        res = client.get(
            "/auth/github/callback?code=x&state=python%7C%E3%81%82",
            cookies={"oauth_state": "abc"}, follow_redirects=False,
        )
        assert res.status_code == 302
        assert res.headers["location"].endswith("/error?code=400")
        assert 'oauth_state=""' in res.headers["set-cookie"]

    def test_nonce一致後にcodeが空でもnonceクッキーを破棄する(self, client):
        res = client.get(
            "/auth/google/callback?state=python%7Cabc",
            cookies={"oauth_state": "abc"}, follow_redirects=False,
        )
        assert res.status_code == 400
        assert res.json()["message"] == "code_required"
        assert 'oauth_state=""' in res.headers["set-cookie"]

    def test_nonce一致後にトークン交換で失敗してもnonceクッキーを破棄する(self, client):
        res = client.get(
            "/auth/github/callback?code=invalid&state=python%7Cabc",
            cookies={"oauth_state": "abc"}, follow_redirects=False,
        )
        assert res.status_code == 302
        assert res.headers["location"].endswith("/error?code=500")
        assert 'oauth_state=""' in res.headers["set-cookie"]
