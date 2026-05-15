"""クライアントエンドポイントのテスト。"""

from tests.conftest import make_client_record, make_staff


class TestIndex:
    def test_クライアント一覧が取得できる(self, client, db_session):
        make_client_record(db_session, identifier="client-001")
        make_client_record(db_session, identifier="client-002", name="別クライアント", email="other@example.com")
        res = client.get("/api/clients")
        assert res.status_code == 200
        data = res.json()
        assert isinstance(data, list)
        assert len(data) == 2

    def test_キーワードでフィルタできる(self, client, db_session):
        make_client_record(db_session, identifier="c-001", name="株式会社テスト")
        make_client_record(db_session, identifier="c-002", name="別会社", email="other@example.com")
        res = client.get("/api/clients?keyword=テスト")
        assert res.status_code == 200
        data = res.json()
        assert len(data) == 1
        assert data[0]["name"] == "株式会社テスト"

    def test_クライアントが存在しない場合空リストを返す(self, client):
        res = client.get("/api/clients")
        assert res.status_code == 200
        assert res.json() == []


class TestShow:
    def test_クライアント詳細が取得できる(self, client, db_session):
        c = make_client_record(db_session)
        res = client.get(f"/api/clients/{c.id}")
        assert res.status_code == 200
        data = res.json()
        assert data["id"] == c.id
        assert data["name"] == c.name

    def test_存在しないIDで404が返る(self, client):
        res = client.get("/api/clients/99999")
        assert res.status_code == 404


class TestStore:
    def test_クライアントが登録できる(self, client):
        payload = {
            "name": "新規テスト株式会社",
            "post_code": "100-0001",
            "pref": "東京都",
            "city": "千代田区",
            "address": "千代田1-1",
            "tel": "0312345678",
            "email": "new@example.com",
        }
        res = client.post("/api/clients/store", json=payload)
        assert res.status_code == 201
        data = res.json()
        assert data["name"] == payload["name"]
        assert data["identifier"] is not None

    def test_name必須バリデーション(self, client):
        res = client.post("/api/clients/store", json={})
        assert res.status_code == 422


class TestUpdate:
    def test_クライアントが更新できる(self, client, db_session):
        c = make_client_record(db_session)
        payload = {"name": "更新後クライアント名"}
        res = client.put(f"/api/clients/{c.id}/update", json=payload)
        assert res.status_code == 200
        assert res.json()["name"] == "更新後クライアント名"

    def test_存在しないIDで404が返る(self, client):
        res = client.put("/api/clients/99999/update", json={"name": "test"})
        assert res.status_code == 404


class TestDestroy:
    def test_クライアントが削除できる(self, client, db_session):
        c = make_client_record(db_session)
        res = client.delete(f"/api/clients/{c.id}/delete")
        assert res.status_code == 200

    def test_存在しないIDで404が返る(self, client):
        res = client.delete("/api/clients/99999/delete")
        assert res.status_code == 404


class TestSoftDelete:
    def test_論理削除済みのクライアントが一覧に含まれる(self, client, db_session):
        c = make_client_record(db_session)
        client.delete(f"/api/clients/{c.id}/delete")
        res = client.get("/api/clients")
        assert res.status_code == 200
        data = res.json()
        assert len(data) == 1

    def test_論理削除済みのクライアント詳細が取得できる(self, client, db_session):
        c = make_client_record(db_session)
        client.delete(f"/api/clients/{c.id}/delete")
        res = client.get(f"/api/clients/{c.id}")
        assert res.status_code == 200
        assert res.json()["id"] == c.id


class TestQr:
    def test_QRコードデータが取得できる(self, client, db_session):
        c = make_client_record(db_session, identifier="abc123")
        res = client.get(f"/api/clients/{c.identifier}/qr")
        assert res.status_code == 200
        data = res.json()
        assert data["identifier"] == "abc123"
        assert data["deeplink_url"] == "authgateway://clients/abc123/info"

    def test_存在しないidentifierで404が返る(self, client):
        res = client.get("/api/clients/nonexistent/qr")
        assert res.status_code == 404


class TestInfo:
    def test_クライアント情報が取得できる(self, client, db_session):
        c = make_client_record(db_session, identifier="info-001", name="テスト店舗", status=2)
        res = client.get(f"/api/clients/{c.identifier}/info")
        assert res.status_code == 200
        data = res.json()
        assert data["identifier"] == "info-001"
        assert data["name"] == "テスト店舗"
        assert data["status"] == 2

    def test_存在しないidentifierで404が返る(self, client):
        res = client.get("/api/clients/nonexistent/info")
        assert res.status_code == 404


class TestStart:
    def test_Inactive状態からActiveに遷移しトークンが返る(self, client, db_session):
        c = make_client_record(db_session, identifier="start-001", status=1)
        res = client.patch(f"/api/clients/{c.identifier}/start")
        assert res.status_code == 200
        data = res.json()
        assert "access_token" in data
        assert data["access_token"] == c.token

    def test_すでにActiveでもトークンが返る(self, client, db_session):
        c = make_client_record(db_session, identifier="start-002", status=2)
        res = client.patch(f"/api/clients/{c.identifier}/start")
        assert res.status_code == 200
        data = res.json()
        assert data["access_token"] == c.token

    def test_Suspended状態からActiveに遷移しトークンが返る(self, client, db_session):
        c = make_client_record(db_session, identifier="start-003", status=3)
        res = client.patch(f"/api/clients/{c.identifier}/start")
        assert res.status_code == 200
        data = res.json()
        assert "access_token" in data

    def test_存在しないidentifierで404が返る(self, client):
        res = client.patch("/api/clients/nonexistent/start")
        assert res.status_code == 404


class TestStop:
    def test_Active状態からSuspendedに遷移する(self, client, db_session):
        c = make_client_record(db_session, identifier="stop-001", status=2)
        res = client.patch(f"/api/clients/{c.identifier}/stop")
        assert res.status_code == 200
        assert res.json() == {}

    def test_Inactive状態では何もせず200を返す(self, client, db_session):
        c = make_client_record(db_session, identifier="stop-002", status=1)
        res = client.patch(f"/api/clients/{c.identifier}/stop")
        assert res.status_code == 200
        assert res.json() == {}

    def test_Suspended状態では何もせず200を返す(self, client, db_session):
        c = make_client_record(db_session, identifier="stop-003", status=3)
        res = client.patch(f"/api/clients/{c.identifier}/stop")
        assert res.status_code == 200
        assert res.json() == {}

    def test_存在しないidentifierで404が返る(self, client):
        res = client.patch("/api/clients/nonexistent/stop")
        assert res.status_code == 404
