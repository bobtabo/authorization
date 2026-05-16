"""管理招待エンドポイントのテスト。"""

from tests.conftest import make_invitation, make_staff


class TestAdminInvitationIndex:
    def test_現在の招待URLが取得できる(self, client, db_session):
        inv = make_invitation(db_session, token="current-token", role=2)
        res = client.get("/api/admin/invitation?role=2")
        assert res.status_code == 200
        data = res.json()
        assert data["token"] == inv.token
        assert "url" in data

    def test_roleパラメータが不正な場合400が返る(self, client):
        res = client.get("/api/admin/invitation?role=3")
        assert res.status_code == 400

    def test_管理者ロールで招待URLが取得できる(self, client, db_session):
        inv = make_invitation(db_session, token="admin-token", role=1)
        res = client.get("/api/admin/invitation?role=1")
        assert res.status_code == 200
        data = res.json()
        assert data["token"] == inv.token


class TestAdminInvitationIssue:
    def test_招待URLが発行できる(self, client, db_session):
        staff = make_staff(db_session)
        res = client.get("/api/admin/invitation/issue?role=2", cookies={"staff_id": str(staff.id)})
        assert res.status_code == 200
        data = res.json()
        assert "url" in data
        assert "token" in data

    def test_再発行で新しいトークンが返る(self, client, db_session):
        staff = make_staff(db_session)
        make_invitation(db_session, token="old-token", role=2)
        res = client.get("/api/admin/invitation/issue?role=2", cookies={"staff_id": str(staff.id)})
        assert res.status_code == 200
        data = res.json()
        assert data["token"] != "old-token"

    def test_未認証で401が返る(self, client):
        res = client.get("/api/admin/invitation/issue?role=2")
        assert res.status_code == 401

    def test_roleパラメータが不正な場合400が返る(self, client, db_session):
        staff = make_staff(db_session)
        res = client.get("/api/admin/invitation/issue?role=3", cookies={"staff_id": str(staff.id)})
        assert res.status_code == 400
