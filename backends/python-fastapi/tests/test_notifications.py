"""通知エンドポイントのテスト。"""

from tests.conftest import make_notification, make_staff


class TestCounts:
    def test_通知件数が取得できる(self, client, db_session):
        staff = make_staff(db_session)
        make_notification(db_session, staff_id=staff.id)
        make_notification(db_session, staff_id=staff.id, read=True)
        res = client.get("/api/notifications/counts", cookies={"staff_id": str(staff.id)})
        assert res.status_code == 200
        data = res.json()
        assert "unread" in data
        assert "total" in data
        assert data["unread"] == 1
        assert data["total"] == 2

    def test_未認証で401が返る(self, client):
        res = client.get("/api/notifications/counts")
        assert res.status_code == 401


class TestIndex:
    def test_通知一覧が取得できる(self, client, db_session):
        staff = make_staff(db_session)
        make_notification(db_session, staff_id=staff.id, title="通知1")
        make_notification(db_session, staff_id=staff.id, title="通知2")
        res = client.get("/api/notifications", cookies={"staff_id": str(staff.id)})
        assert res.status_code == 200
        data = res.json()
        assert "items" in data
        assert len(data["items"]) == 2

    def test_未認証で401が返る(self, client):
        res = client.get("/api/notifications")
        assert res.status_code == 401


class TestStore:
    def test_通知トリガーが受け付けられる(self, client):
        payload = {"title": "新規通知", "body": "通知本文"}
        res = client.post("/api/notifications", json=payload)
        assert res.status_code == 202
        data = res.json()
        assert data["message"] == "notification_accepted"


class TestBulkRead:
    def test_一括既読が成功する(self, client, db_session):
        staff = make_staff(db_session)
        n1 = make_notification(db_session, staff_id=staff.id)
        n2 = make_notification(db_session, staff_id=staff.id)
        res = client.patch(
            "/api/notifications",
            json={"ids": [n1.id, n2.id], "executor_id": staff.id},
        )
        assert res.status_code == 200
        assert "updated" in res.json()


class TestRead:
    def test_単一通知が既読になる(self, client, db_session):
        staff = make_staff(db_session)
        n = make_notification(db_session, staff_id=staff.id)
        res = client.patch(f"/api/notifications/{n.id}", json={"read": True})
        assert res.status_code == 200
        assert res.json()["id"] == n.id
