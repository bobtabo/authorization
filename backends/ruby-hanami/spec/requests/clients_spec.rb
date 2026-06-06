# frozen_string_literal: true

require "spec_helper"

RSpec.describe "Clients" do
  before { truncate_tables }

  describe "GET /api/clients" do
    it "クライアント一覧を返す" do
      create_client(identifier: "c-001", email: "c1@example.com")
      create_client(identifier: "c-002", email: "c2@example.com")
      get "/api/clients"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["data"]).to be_an(Array)
      expect(body["data"].size).to eq(2)
    end

    it "クライアントが存在しない場合空リストを返す" do
      get "/api/clients"
      expect(last_response.status).to eq(200)
      expect(JSON.parse(last_response.body)["data"]).to eq([])
    end
  end

  describe "GET /api/clients/:id" do
    it "クライアント詳細を返す" do
      client = create_client
      get "/api/clients/#{client[:id]}"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["name"]).to eq(client[:name])
      expect(body["identifier"]).to eq(client[:identifier])
    end

    it "存在しないIDでエラーを返す" do
      get "/api/clients/99999"
      expect(last_response.status).not_to eq(200)
    end
  end

  describe "POST /api/clients/store" do
    it "クライアントを登録して201を返す" do
      staff = create_staff
      payload = {
        name:      "新規クライアント株式会社",
        post_code: "100-0001",
        pref:      "東京都",
        city:      "千代田区",
        address:   "千代田1-1",
        tel:       "0312345678",
        email:     "new-client@example.com",
      }
      post "/api/clients/store", payload.to_json,
           { "CONTENT_TYPE" => "application/json", "HTTP_COOKIE" => "staff_id=#{staff[:id]}" }
      expect(last_response.status).to eq(201)
      body = JSON.parse(last_response.body)
      expect(body["id"]).not_to be_nil
    end
  end

  describe "PUT /api/clients/:id/update" do
    it "クライアントを更新して詳細を返す" do
      staff  = create_staff
      client = create_client
      put "/api/clients/#{client[:id]}/update",
          { name: "更新後クライアント名", version: client[:version] }.to_json,
          { "CONTENT_TYPE" => "application/json", "HTTP_COOKIE" => "staff_id=#{staff[:id]}" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["name"]).to eq("更新後クライアント名")
    end
  end

  describe "DELETE /api/clients/:id/delete" do
    it "クライアントを削除して200を返す" do
      staff  = create_staff
      client = create_client
      delete "/api/clients/#{client[:id]}/delete",
             { version: client[:version] }.to_json,
             { "CONTENT_TYPE" => "application/json", "HTTP_COOKIE" => "staff_id=#{staff[:id]}" }
      expect(last_response.status).to eq(200)
    end
  end

  describe "GET /api/clients/:identifier/qr" do
    it "QRコードデータを返す" do
      client = create_client(identifier: "qr-test-001")
      get "/api/clients/qr-test-001/qr"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["identifier"]).to eq("qr-test-001")
      expect(body["deeplink_url"]).to eq("authgateway://clients/qr-test-001/info")
    end

    it "存在しない identifier で404を返す" do
      get "/api/clients/not-exist/qr"
      expect(last_response.status).to eq(404)
    end
  end

  describe "GET /api/clients/:identifier/info" do
    it "クライアント情報を返す" do
      client = create_client(identifier: "info-test-001", status: 2)
      get "/api/clients/info-test-001/info"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["identifier"]).to eq("info-test-001")
      expect(body["name"]).to eq(client[:name])
      expect(body["status"]).to eq(2)
    end

    it "存在しない identifier で404を返す" do
      get "/api/clients/not-exist/info"
      expect(last_response.status).to eq(404)
    end
  end

  describe "PATCH /api/clients/:identifier/start" do
    it "Inactive クライアントを Active に遷移しアクセストークンを返す" do
      client = create_client(identifier: "start-test-001", status: 1)
      patch "/api/clients/start-test-001/start", {}.to_json,
            { "CONTENT_TYPE" => "application/json" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["access_token"]).to eq(client[:access_token])
      updated = db[:clients].where(identifier: "start-test-001").first
      expect(updated[:status]).to eq(2)
      expect(updated[:start_at]).not_to be_nil
      expect(updated[:stop_at]).to be_nil
    end

    it "既に Active のクライアントでもアクセストークンを返す" do
      client = create_client(identifier: "start-test-002", status: 2)
      patch "/api/clients/start-test-002/start", {}.to_json,
            { "CONTENT_TYPE" => "application/json" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["access_token"]).to eq(client[:access_token])
    end

    it "存在しない identifier で404を返す" do
      patch "/api/clients/not-exist/start", {}.to_json,
            { "CONTENT_TYPE" => "application/json" }
      expect(last_response.status).to eq(404)
    end
  end

  describe "PATCH /api/clients/:identifier/stop" do
    it "Active クライアントを Suspended に遷移する" do
      create_client(identifier: "stop-test-001", status: 2)
      patch "/api/clients/stop-test-001/stop", {}.to_json,
            { "CONTENT_TYPE" => "application/json" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body).to eq({})
      updated = db[:clients].where(identifier: "stop-test-001").first
      expect(updated[:status]).to eq(3)
      expect(updated[:stop_at]).not_to be_nil
    end

    it "Active 以外のクライアントは何もせず200を返す" do
      create_client(identifier: "stop-test-002", status: 1)
      patch "/api/clients/stop-test-002/stop", {}.to_json,
            { "CONTENT_TYPE" => "application/json" }
      expect(last_response.status).to eq(200)
      updated = db[:clients].where(identifier: "stop-test-002").first
      expect(updated[:status]).to eq(1)
    end

    it "存在しない identifier で404を返す" do
      patch "/api/clients/not-exist/stop", {}.to_json,
            { "CONTENT_TYPE" => "application/json" }
      expect(last_response.status).to eq(404)
    end
  end
end
