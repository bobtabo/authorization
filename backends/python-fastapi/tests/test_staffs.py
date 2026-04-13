"""スタッフエンドポイントのテスト。"""

from tests.conftest import make_staff


class TestIndex:
    def test_スタッフ一覧が取得できる(self, client, db_session):
        make_staff(db_session, email="staff1@example.com")
        make_staff(db_session, email="staff2@example.com", name="別スタッフ", role=2)
        res = client.get("/api/staffs")
        assert res.status_code == 200
        data = res.json()
        assert "items" in data
        assert len(data["items"]) == 2

    def test_キーワードでフィルタできる(self, client, db_session):
        make_staff(db_session, email="admin@example.com", name="管理者スタッフ")
        make_staff(db_session, email="member@example.com", name="メンバースタッフ")
        res = client.get("/api/staffs?keyword=管理者")
        assert res.status_code == 200
        items = res.json()["items"]
        assert len(items) == 1
        assert items[0]["name"] == "管理者スタッフ"

    def test_スタッフが存在しない場合空リストを返す(self, client):
        res = client.get("/api/staffs")
        assert res.status_code == 200
        assert res.json()["items"] == []


class TestUpdateRole:
    def test_ロールが更新できる(self, client, db_session):
        staff = make_staff(db_session, role=2)
        executor = make_staff(db_session, email="executor@example.com", role=1)
        res = client.patch(
            f"/api/staffs/{staff.id}/updateRole",
            json={"role": 1},
            cookies={"staff_id": str(executor.id)},
        )
        assert res.status_code == 200
        assert res.json()["id"] == staff.id

    def test_存在しないIDで404が返る(self, client, db_session):
        executor = make_staff(db_session)
        res = client.patch(
            "/api/staffs/99999/updateRole",
            json={"role": 1},
            cookies={"staff_id": str(executor.id)},
        )
        assert res.status_code == 404


class TestDestroy:
    def test_スタッフが削除できる(self, client, db_session):
        staff = make_staff(db_session, email="target@example.com")
        executor = make_staff(db_session, email="executor@example.com")
        res = client.delete(
            f"/api/staffs/{staff.id}/delete",
            cookies={"staff_id": str(executor.id)},
        )
        assert res.status_code == 200
        assert res.json()["id"] == staff.id

    def test_存在しないIDで404が返る(self, client, db_session):
        executor = make_staff(db_session)
        res = client.delete(
            "/api/staffs/99999/delete",
            cookies={"staff_id": str(executor.id)},
        )
        assert res.status_code == 404
