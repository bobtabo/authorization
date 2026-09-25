"""スタッフエンドポイントのテスト。"""

from datetime import datetime

from tests.conftest import make_staff, sign_staff_cookie


class TestIndex:
    def test_スタッフ一覧が取得できる(self, client, db_session):
        make_staff(db_session, email="staff1@example.com")
        make_staff(db_session, email="staff2@example.com", name="別スタッフ", role=2)
        res = client.get("/api/staffs")
        assert res.status_code == 200
        body = res.json()
        assert "data" in body
        assert "pager" in body
        assert len(body["data"]) == 2

    def test_キーワードでフィルタできる(self, client, db_session):
        make_staff(db_session, email="admin@example.com", name="管理者スタッフ")
        make_staff(db_session, email="member@example.com", name="メンバースタッフ")
        res = client.get("/api/staffs?keyword=管理者")
        assert res.status_code == 200
        data = res.json()["data"]
        assert len(data) == 1
        assert data[0]["name"] == "管理者スタッフ"

    def test_スタッフが存在しない場合空リストを返す(self, client):
        res = client.get("/api/staffs")
        assert res.status_code == 200
        assert res.json()["data"] == []

    def test_keywordの_はワイルドカードとして解釈されない(self, client, db_session):
        make_staff(db_session, name="アンダースコア", email="a_b@example.com")
        make_staff(db_session, name="エックス", email="axb@example.com")
        res = client.get("/api/staffs", params={"keyword": "a_b"})
        assert res.status_code == 200
        data = res.json()["data"]
        assert len(data) == 1
        assert data[0]["name"] == "アンダースコア"


class TestUpdateRole:
    def test_ロールが更新できる(self, client, db_session):
        staff = make_staff(db_session, role=2)
        executor = make_staff(db_session, email="executor@example.com", role=1)
        res = client.patch(
            f"/api/staffs/{staff.id}/updateRole",
            json={"role": 1, "version": staff.version},
            cookies={"staff_id": sign_staff_cookie(executor.id)},
        )
        assert res.status_code == 200
        assert res.json()["id"] == staff.id

    def test_存在しないIDで404が返る(self, client, db_session):
        executor = make_staff(db_session)
        res = client.patch(
            "/api/staffs/99999/updateRole",
            json={"role": 1},
            cookies={"staff_id": sign_staff_cookie(executor.id)},
        )
        assert res.status_code == 404

    def test_未認証で401が返る(self, client, db_session):
        staff = make_staff(db_session, email="target-unauth@example.com", role=2)
        res = client.patch(
            f"/api/staffs/{staff.id}/updateRole",
            json={"role": 1, "version": staff.version},
        )
        assert res.status_code == 401

    def test_Admin以外の実行者では403が返る(self, client, db_session):
        staff = make_staff(db_session, email="target-member@example.com", role=2)
        executor = make_staff(db_session, email="member-executor@example.com", role=2)
        res = client.patch(
            f"/api/staffs/{staff.id}/updateRole",
            json={"role": 1, "version": staff.version},
            cookies={"staff_id": sign_staff_cookie(executor.id)},
        )
        assert res.status_code == 403

    def test_無効化済みAdminの実行者では403が返る(self, client, db_session):
        staff = make_staff(db_session, email="target-deleted-admin@example.com", role=2)
        # 署名済みクッキーは有効だが、実行者は既に無効化（論理削除）されている状態を再現する。
        executor = make_staff(db_session, email="deleted-admin-executor@example.com", role=1, deleted_at=datetime.now())
        res = client.patch(
            f"/api/staffs/{staff.id}/updateRole",
            json={"role": 1, "version": staff.version},
            cookies={"staff_id": sign_staff_cookie(executor.id)},
        )
        assert res.status_code == 403

    def test_Admin以外の実行者が自分自身を更新しても403が返る(self, client, db_session):
        # 自分自身の更新チェック（400）より先にAdmin検証（403）が行われることを確認する。
        executor = make_staff(db_session, email="self-update-member@example.com", role=2)
        res = client.patch(
            f"/api/staffs/{executor.id}/updateRole",
            json={"role": 1, "version": executor.version},
            cookies={"staff_id": sign_staff_cookie(executor.id)},
        )
        assert res.status_code == 403

    def test_Adminが自分自身を更新すると400が返る(self, client, db_session):
        executor = make_staff(db_session, email="self-update-admin@example.com", role=1)
        res = client.patch(
            f"/api/staffs/{executor.id}/updateRole",
            json={"role": 2, "version": executor.version},
            cookies={"staff_id": sign_staff_cookie(executor.id)},
        )
        assert res.status_code == 400


class TestDestroy:
    def test_スタッフが削除できる(self, client, db_session):
        staff = make_staff(db_session, email="target@example.com")
        executor = make_staff(db_session, email="executor@example.com")
        res = client.request(
            "DELETE",
            f"/api/staffs/{staff.id}/delete",
            json={"version": staff.version},
            cookies={"staff_id": sign_staff_cookie(executor.id)},
        )
        assert res.status_code == 200
        assert res.json()["id"] == staff.id

    def test_存在しないIDで404が返る(self, client, db_session):
        executor = make_staff(db_session)
        res = client.delete(
            "/api/staffs/99999/delete",
            cookies={"staff_id": sign_staff_cookie(executor.id)},
        )
        assert res.status_code == 404
