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

  describe "OAuth state nonce" do
    it "リダイレクト時に nonce を含む state と oauth_state クッキーを発行する" do
      get "/auth/github/redirect?token=inv-token"
      expect(last_response.status).to eq(302)
      cookie = Array(last_response.headers["Set-Cookie"]).join("\n")
      nonce = cookie[/oauth_state=([0-9a-f]+)/, 1]
      expect(nonce).not_to be_nil
      expect(cookie).to include("HttpOnly")
      expect(last_response.headers["Location"]).to include(CGI.escape("rb-hanami|#{nonce}|inv-token"))
    end

    it "oauth_state クッキーが無いコールバックを 400 にする" do
      get "/auth/github/callback?code=abc&state=hanami%7Cnonce123"
      expect(last_response.status).to eq(302)
      expect(last_response.headers["Location"]).to include("/error?code=400")
    end

    it "nonce が一致しないコールバックを 400 にしクッキーを破棄する" do
      get "/auth/google/callback?code=abc&state=hanami%7Cwrong", {}, { "HTTP_COOKIE" => "oauth_state=right" }
      expect(last_response.status).to eq(302)
      expect(last_response.headers["Location"]).to include("/error?code=400")
      expect(Array(last_response.headers["Set-Cookie"]).join("\n")).to include("oauth_state=; Path=/; HttpOnly; Max-Age=0")
    end

    it "state に nonce セグメントが無いコールバックを 400 にする" do
      get "/auth/github/callback?code=abc&state=hanami", {}, { "HTTP_COOKIE" => "oauth_state=nonce123" }
      expect(last_response.headers["Location"]).to include("/error?code=400")
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
