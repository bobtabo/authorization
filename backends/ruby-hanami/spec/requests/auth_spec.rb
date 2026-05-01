# frozen_string_literal: true

require "spec_helper"

RSpec.describe "Auth" do
  before { truncate_tables }

  describe "GET /api/auth/me" do
    it "認証済みでプロフィールを返す" do
      staff = create_staff
      get "/api/auth/me", {}, { "HTTP_COOKIE" => "staff_id=#{staff[:id]}" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["staff_id"]).to eq(staff[:id])
      expect(body["name"]).to eq(staff[:name])
    end

    it "未認証で401を返す" do
      get "/api/auth/me"
      expect(last_response.status).to eq(401)
    end
  end

  describe "GET /api/auth/login" do
    it "認証済みでログイン情報を返す" do
      staff = create_staff
      get "/api/auth/login", {}, { "HTTP_COOKIE" => "staff_id=#{staff[:id]}" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["staff_id"]).to eq(staff[:id])
    end

    it "未認証で401を返す" do
      get "/api/auth/login"
      expect(last_response.status).to eq(401)
    end
  end

  describe "GET /api/auth/logout" do
    it "200を返す" do
      get "/api/auth/logout"
      expect(last_response.status).to eq(200)
    end
  end

  describe "GET /api/auth/invitation/:token" do
    it "有効なトークンで招待情報を返す" do
      inv = create_invitation("test-token-xyz")
      get "/api/auth/invitation/#{inv[:token]}"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["token"]).to eq(inv[:token])
      expect(body["found"]).to be true
    end
  end
end
