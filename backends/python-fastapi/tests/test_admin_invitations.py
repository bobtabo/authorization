"""管理招待エンドポイントのテスト。"""

from tests.conftest import make_invitation


class TestAdminInvitationIssue:
    def test_招待URLが発行できる(self, client):
        res = client.get("/api/admin/invitation/issue")
        assert res.status_code == 200
        data = res.json()
        assert "url" in data
        assert "token" in data

    def test_再発行で新しいトークンが返る(self, client, db_session):
        make_invitation(db_session, token="old-token")
        res = client.get("/api/admin/invitation/issue")
        assert res.status_code == 200
        data = res.json()
        assert data["token"] != "old-token"
