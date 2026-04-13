"""認可ゲートエンドポイントのテスト。"""

from tests.conftest import make_client_record


class TestIssue:
    def test_JWTが発行できる(self, client, db_session):
        c = make_client_record(db_session, status=2)
        res = client.get(
            "/api/gate/issue?member=member-001",
            headers={"Authorization": f"Bearer {c.token}"},
        )
        assert res.status_code == 200
        assert "token" in res.json()

    def test_利用中以外のクライアントで401が返る(self, client, db_session):
        c = make_client_record(db_session, identifier="suspended-client", status=3)
        res = client.get(
            "/api/gate/issue?member=member-001",
            headers={"Authorization": f"Bearer {c.token}"},
        )
        assert res.status_code == 401

    def test_memberパラメーター未指定で422が返る(self, client, db_session):
        c = make_client_record(db_session, status=2)
        res = client.get(
            "/api/gate/issue",
            headers={"Authorization": f"Bearer {c.token}"},
        )
        assert res.status_code == 422

    def test_無効なトークンで401が返る(self, client):
        res = client.get(
            "/api/gate/issue?member=member-001",
            headers={"Authorization": "Bearer invalid-token"},
        )
        assert res.status_code == 401


class TestVerify:
    def test_JWTが検証できる(self, client, db_session):
        c = make_client_record(db_session, status=2)
        # JWT を発行
        issue_res = client.get(
            "/api/gate/issue?member=member-001",
            headers={"Authorization": f"Bearer {c.token}"},
        )
        assert issue_res.status_code == 200
        jwt_token = issue_res.json()["token"]

        # JWT を検証
        res = client.get(f"/api/gate/client/{c.identifier}/verify?token={jwt_token}")
        assert res.status_code == 200
        data = res.json()
        assert data["identifier"] == c.identifier
        assert data["member"] == "member-001"

    def test_tokenパラメーター未指定で422が返る(self, client, db_session):
        c = make_client_record(db_session, status=2)
        res = client.get(f"/api/gate/client/{c.identifier}/verify")
        assert res.status_code == 422

    def test_存在しないidentifierで404が返る(self, client):
        res = client.get("/api/gate/client/unknown-client/verify?token=dummy")
        assert res.status_code == 404

    def test_無効なJWTで401が返る(self, client, db_session):
        make_client_record(db_session, identifier="test-client-jwt", status=2)
        res = client.get("/api/gate/client/test-client-jwt/verify?token=invalid.jwt.token")
        assert res.status_code == 401
