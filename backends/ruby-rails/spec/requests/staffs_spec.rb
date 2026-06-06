# frozen_string_literal: true

require "rails_helper"

RSpec.describe "Staffs", type: :request do
  describe "GET /api/staffs" do
    it "スタッフ一覧を返す" do
      create_staff(email: "s1@example.com")
      create_staff(email: "s2@example.com")
      get "/api/staffs"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["data"]).to be_an(Array)
      expect(body["data"].size).to eq(2)
      expect(body["pager"]).to be_a(Hash)
    end

    it "スタッフが存在しない場合空リストを返す" do
      get "/api/staffs"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["data"]).to eq([])
    end
  end

  describe "PATCH /api/staffs/:id/updateRole" do
    it "ロールを更新して id を返す" do
      target   = create_staff(email: "target@example.com", role: 2)
      executor = create_staff(email: "exec@example.com",   role: 1)
      patch "/api/staffs/#{target.id}/updateRole",
            params: { role: 1 }.to_json,
            headers: { "Content-Type" => "application/json", "Cookie" => "staff_id=#{executor.id}" }
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["id"]).to eq(target.id)
    end
  end

  describe "PATCH /api/staffs/:id/restore" do
    it "削除済みスタッフを復元して id を返す" do
      staff = create_staff
      staff.update!(deleted_at: Time.current)
      patch "/api/staffs/#{staff.id}/restore"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["id"]).to eq(staff.id)
    end
  end

  describe "DELETE /api/staffs/:id/delete" do
    it "スタッフを論理削除して id を返す" do
      executor = create_staff(email: "exec@example.com")
      target   = create_staff(email: "target@example.com")
      delete "/api/staffs/#{target.id}/delete",
             params:  { version: 1 }.to_json,
             headers: { "Cookie" => "staff_id=#{executor.id}", "Content-Type" => "application/json" }
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["id"]).to eq(target.id)
    end
  end
end
