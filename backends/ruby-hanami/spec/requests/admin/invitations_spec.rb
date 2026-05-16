# frozen_string_literal: true

require "spec_helper"

RSpec.describe "Admin::Invitations" do
  before { truncate_tables }

  describe "GET /api/admin/invitation" do
    it "デフォルト(role=2)で現在の招待情報を返す" do
      inv = create_invitation(role: 2)
      get "/api/admin/invitation"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["found"]).to be true
      expect(body["token"]).to eq(inv[:token].to_s)
    end

    it "role=1 で管理者招待情報を返す" do
      inv = create_invitation(role: 1)
      get "/api/admin/invitation?role=1"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["found"]).to be true
      expect(body["token"]).to eq(inv[:token].to_s)
    end

    it "role が不正な値なら 400 を返す" do
      get "/api/admin/invitation?role=3"
      expect(last_response.status).to eq(400)
    end
  end

  describe "GET /api/admin/invitation/issue" do
    it "認証クッキーがなければ 401 を返す" do
      get "/api/admin/invitation/issue"
      expect(last_response.status).to eq(401)
    end

    it "認証クッキーがあれば新しい招待を発行して返す" do
      staff = create_staff
      set_cookie "staff_id=#{staff[:id]}"
      get "/api/admin/invitation/issue"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["found"]).to be true
      expect(body["token"]).not_to be_nil
    end

    it "role=1 で管理者招待を発行する" do
      staff = create_staff
      set_cookie "staff_id=#{staff[:id]}"
      get "/api/admin/invitation/issue?role=1"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["found"]).to be true
    end

    it "role が不正な値なら 400 を返す" do
      staff = create_staff
      set_cookie "staff_id=#{staff[:id]}"
      get "/api/admin/invitation/issue?role=0"
      expect(last_response.status).to eq(400)
    end
  end
end
