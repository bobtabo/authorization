# frozen_string_literal: true

require "spec_helper"

RSpec.describe "Staffs" do
  before { truncate_tables }

  describe "GET /api/staffs" do
    it "スタッフ一覧を返す" do
      create_staff(email: "s1@example.com")
      create_staff(email: "s2@example.com")
      get "/api/staffs"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["data"]).to be_an(Array)
      expect(body["data"].size).to eq(2)
      expect(body["pager"]).to be_a(Hash)
    end

    it "スタッフが存在しない場合空リストを返す" do
      get "/api/staffs"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["data"]).to eq([])
    end

    it "keywordの_はワイルドカードとして解釈されない" do
      create_staff(name: "アンダースコア", email: "a_b@example.com")
      create_staff(name: "エックス", email: "axb@example.com")
      get "/api/staffs", { keyword: "a_b" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["data"].size).to eq(1)
    end
  end

  describe "PATCH /api/staffs/:id/updateRole" do
    it "ロールを更新して id を返す" do
      target   = create_staff(email: "target@example.com", role: 2)
      executor = create_staff(email: "exec@example.com",   role: 1)
      patch "/api/staffs/#{target[:id]}/updateRole",
            { role: 1, version: target[:version] }.to_json,
            { "CONTENT_TYPE" => "application/json", "HTTP_COOKIE" => "staff_id=#{sign_staff_cookie(executor[:id])}" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(target[:id])
    end

    it "未認証の場合401を返す" do
      target = create_staff(email: "target-unauth@example.com", role: 2)
      patch "/api/staffs/#{target[:id]}/updateRole",
            { role: 1, version: target[:version] }.to_json,
            { "CONTENT_TYPE" => "application/json" }
      expect(last_response.status).to eq(401)
    end

    it "実行者がAdmin以外の場合403を返す" do
      target   = create_staff(email: "target-nonadmin@example.com", role: 2)
      executor = create_staff(email: "exec-nonadmin@example.com",   role: 2)
      patch "/api/staffs/#{target[:id]}/updateRole",
            { role: 1, version: target[:version] }.to_json,
            { "CONTENT_TYPE" => "application/json", "HTTP_COOKIE" => "staff_id=#{sign_staff_cookie(executor[:id])}" }
      expect(last_response.status).to eq(403)
    end

    it "実行者が無効化済みAdminの場合403を返す" do
      target   = create_staff(email: "target-deletedadmin@example.com", role: 2)
      executor = create_staff(email: "exec-deletedadmin@example.com",   role: 1)
      db[:staffs].where(id: executor[:id]).update(deleted_at: Time.now, updated_at: Time.now)
      patch "/api/staffs/#{target[:id]}/updateRole",
            { role: 1, version: target[:version] }.to_json,
            { "CONTENT_TYPE" => "application/json", "HTTP_COOKIE" => "staff_id=#{sign_staff_cookie(executor[:id])}" }
      expect(last_response.status).to eq(403)
    end
  end

  describe "PATCH /api/staffs/:id/restore" do
    it "削除済みスタッフを復元して id を返す" do
      staff = create_staff
      db[:staffs].where(id: staff[:id]).update(deleted_at: Time.now, updated_at: Time.now)
      current_version = db[:staffs].where(id: staff[:id]).first[:version]
      patch "/api/staffs/#{staff[:id]}/restore",
            { version: current_version }.to_json,
            { "CONTENT_TYPE" => "application/json" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(staff[:id])
    end
  end

  describe "DELETE /api/staffs/:id/delete" do
    it "スタッフを論理削除して id を返す" do
      executor = create_staff(email: "exec@example.com")
      target   = create_staff(email: "target@example.com")
      delete "/api/staffs/#{target[:id]}/delete",
             { version: target[:version] }.to_json,
             { "CONTENT_TYPE" => "application/json", "HTTP_COOKIE" => "staff_id=#{sign_staff_cookie(executor[:id])}" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(target[:id])
    end
  end
end
