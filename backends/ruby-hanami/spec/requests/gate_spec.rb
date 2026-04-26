# frozen_string_literal: true

require "spec_helper"

RSpec.describe "Gate" do
  before { truncate_tables }

  describe "GET /api/gate/issue" do
    context "member パラメータあり" do
      it "JWT トークンを返す" do
        client = create_client
        get "/api/gate/issue?member=user-001",
            {}, { "HTTP_AUTHORIZATION" => "Bearer #{client[:access_token]}" }
        expect(last_response.status).to eq(200)
        body = JSON.parse(last_response.body)
        expect(body["token"]).not_to be_nil
      end
    end

    context "member パラメータなし" do
      it "400を返す" do
        get "/api/gate/issue"
        expect(last_response.status).to eq(400)
        body = JSON.parse(last_response.body)
        expect(body["error"]).to eq("member_required")
      end
    end
  end

  describe "GET /api/gate/client/:identifier/verify" do
    context "有効なトークンの場合" do
      it "ペイロードを返す" do
        client = create_client
        get "/api/gate/issue?member=user-001",
            {}, { "HTTP_AUTHORIZATION" => "Bearer #{client[:access_token]}" }
        token = JSON.parse(last_response.body)["token"]

        get "/api/gate/client/#{client[:identifier]}/verify?token=#{token}"
        expect(last_response.status).to eq(200)
        body = JSON.parse(last_response.body)
        expect(body["sub"]).to eq("user-001")
      end
    end

    context "token パラメータなし" do
      it "400を返す" do
        client = create_client
        get "/api/gate/client/#{client[:identifier]}/verify"
        expect(last_response.status).to eq(400)
        body = JSON.parse(last_response.body)
        expect(body["error"]).to eq("token_required")
      end
    end
  end
end
