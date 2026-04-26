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
      expect(body["items"]).to be_an(Array)
      expect(body["items"].size).to eq(2)
    end

    it "スタッフが存在しない場合空リストを返す" do
      get "/api/staffs"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["items"]).to eq([])
    end
  end

  describe "PATCH /api/staffs/:id/updateRole" do
    it "ロールを更新して id を返す" do
      target   = create_staff(email: "target@example.com", role: 2)
      executor = create_staff(email: "exec@example.com",   role: 1)
      patch "/api/staffs/#{target[:id]}/updateRole",
            { role: 1 }.to_json,
            { "CONTENT_TYPE" => "application/json", "HTTP_COOKIE" => "staff_id=#{executor[:id]}" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(target[:id])
    end
  end

  describe "PATCH /api/staffs/:id/restore" do
    it "削除済みスタッフを復元して id を返す" do
      staff = create_staff
      db[:staffs].where(id: staff[:id]).update(deleted_at: Time.now, updated_at: Time.now)
      patch "/api/staffs/#{staff[:id]}/restore"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(staff[:id])
    end
  end

  describe "DELETE /api/staffs/:id/delete" do
    it "スタッフを論理削除して id を返す" do
      executor = create_staff(email: "exec@example.com")
      target   = create_staff(email: "target@example.com")
      delete "/api/staffs/#{target[:id]}/delete", {},
             { "HTTP_COOKIE" => "staff_id=#{executor[:id]}" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(target[:id])
    end
  end
end
