# frozen_string_literal: true

require "rails_helper"

RSpec.describe "Admin::Invitations", type: :request do
  describe "GET /api/admin/invitation" do
    it "現在の招待情報を返す" do
      inv = create_invitation
      get "/api/admin/invitation"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["found"]).to be true
      expect(body["token"]).to eq(inv.token)
    end
  end

  describe "GET /api/admin/invitation/issue" do
    it "新しい招待を発行して返す" do
      get "/api/admin/invitation/issue"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["found"]).to be true
      expect(body["token"]).not_to be_nil
    end
  end
end
