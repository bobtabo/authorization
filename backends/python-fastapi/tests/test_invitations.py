"""招待エンドポイントのテスト。"""

from tests.conftest import make_invitation


class TestIssue:
    def test_招待URLが発行できる(self, client):
        res = client.get("/api/invitation/issue")
        assert res.status_code == 200
        data = res.json()
        assert "url" in data
        assert "token" in data

    def test_再発行で新しいトークンが返る(self, client, db_session):
        make_invitation(db_session, token="old-token")
        res = client.get("/api/invitation/issue")
        assert res.status_code == 200
        data = res.json()
        assert data["token"] != "old-token"


class TestIndex:
    def test_現在の招待URLが取得できる(self, client, db_session):
        inv = make_invitation(db_session, token="current-token")
        res = client.get("/api/invitation")
        assert res.status_code == 200
        data = res.json()
        assert data["token"] == inv.token
        assert "url" in data
