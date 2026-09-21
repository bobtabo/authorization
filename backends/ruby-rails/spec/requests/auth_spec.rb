# frozen_string_literal: true

require "rails_helper"

RSpec.describe "Auth", type: :request do
  describe "GET /api/auth/me" do
    it "認証済みでプロフィールを返す" do
      staff = create_staff
      get "/api/auth/me", headers: { "Cookie" => "staff_id=#{staff.id}" }
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["staff_id"]).to eq(staff.id)
      expect(body["name"]).to eq(staff.name)
    end

    it "未認証で401を返す" do
      get "/api/auth/me"
      expect(response).to have_http_status(401)
    end
  end

  describe "GET /api/auth/login" do
    it "認証済みでログイン情報を返す" do
      staff = create_staff
      get "/api/auth/login", headers: { "Cookie" => "staff_id=#{staff.id}" }
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["staff_id"]).to eq(staff.id)
    end

    it "未認証で401を返す" do
      get "/api/auth/login"
      expect(response).to have_http_status(401)
    end
  end

  describe "GET /api/auth/logout" do
    it "200を返す" do
      get "/api/auth/logout"
      expect(response).to have_http_status(200)
    end
  end

  describe "GET /api/auth/invitation/:token" do
    it "有効なトークンで招待情報を返す" do
      inv = create_invitation("test-token-xyz")
      get "/api/auth/invitation/#{inv.token}"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["token"]).to eq(inv.token)
      expect(body["found"]).to be true
    end
  end

  describe "OAuth state nonce" do
    it "認可開始時にnonceクッキーを発行しstateに含める" do
      get "/auth/google/redirect", params: { token: "inv-token" }
      expect(response).to have_http_status(302)
      nonce = cookies["oauth_state"]
      expect(nonce).to be_present
      expect(response.headers["Set-Cookie"].to_s).to match(/httponly/i)
      state = CGI.parse(URI(response.location).query)["state"].first
      expect(state).to eq("rb-rails|#{nonce}|inv-token")
    end

    it "nonceクッキーが無い場合は400エラーページへリダイレクトする" do
      get "/auth/google/callback", params: { code: "x", state: "rb-rails|abc" }
      expect(response).to have_http_status(302)
      expect(response.location).to end_with("/error?code=400")
    end

    it "nonceが一致しない場合は400エラーページへリダイレクトする" do
      get "/auth/github/callback", params: { code: "x", state: "rb-rails|abc" },
                                   headers: { "Cookie" => "oauth_state=xyz" }
      expect(response).to have_http_status(302)
      expect(response.location).to end_with("/error?code=400")
    end
  end
end
