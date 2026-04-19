"""招待エンドポイントのテスト。"""

from tests.conftest import make_invitation


class TestIndex:
    def test_現在の招待URLが取得できる(self, client, db_session):
        inv = make_invitation(db_session, token="current-token")
        res = client.get("/api/invitation")
        assert res.status_code == 200
        data = res.json()
        assert data["token"] == inv.token
        assert "url" in data
