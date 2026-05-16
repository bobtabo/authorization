# frozen_string_literal: true

require "rails_helper"

RSpec.describe "Admin::Invitations", type: :request do
  describe "GET /api/admin/invitation" do
    it "現在の招待情報を返す（デフォルト role=2）" do
      inv = create_invitation(SecureRandom.hex(16), 2)
      get "/api/admin/invitation"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["found"]).to be true
      expect(body["token"]).to eq(inv.token)
    end

    it "role=1 を指定すると管理者向け招待情報を返す" do
      inv = create_invitation(SecureRandom.hex(16), 1)
      get "/api/admin/invitation?role=1"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["found"]).to be true
      expect(body["token"]).to eq(inv.token)
    end

    it "role が不正なら 400 を返す" do
      get "/api/admin/invitation?role=3"
      expect(response).to have_http_status(400)
    end
  end

  describe "GET /api/admin/invitation/issue" do
    it "新しい招待を発行して返す（デフォルト role=2）" do
      get "/api/admin/invitation/issue"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["found"]).to be true
      expect(body["token"]).not_to be_nil
    end

    it "role=1 を指定すると管理者向け招待を発行して返す" do
      get "/api/admin/invitation/issue?role=1"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["found"]).to be true
      expect(body["token"]).not_to be_nil
    end

    it "role が不正なら 400 を返す" do
      get "/api/admin/invitation/issue?role=0"
      expect(response).to have_http_status(400)
    end
  end
end
