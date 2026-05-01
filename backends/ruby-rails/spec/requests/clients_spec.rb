# frozen_string_literal: true

require "rails_helper"

RSpec.describe "Clients", type: :request do
  describe "GET /api/clients" do
    it "クライアント一覧を返す" do
      create_client(identifier: "c-001", email: "c1@example.com")
      create_client(identifier: "c-002", email: "c2@example.com")
      get "/api/clients"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body).to be_an(Array)
      expect(body.size).to eq(2)
    end

    it "クライアントが存在しない場合空リストを返す" do
      get "/api/clients"
      expect(response).to have_http_status(200)
      expect(JSON.parse(response.body)).to eq([])
    end
  end

  describe "GET /api/clients/:id" do
    it "クライアント詳細を返す" do
      client = create_client
      get "/api/clients/#{client.id}"
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["name"]).to eq(client.name)
      expect(body["identifier"]).to eq(client.identifier)
    end

    it "存在しないIDでエラーを返す" do
      get "/api/clients/99999"
      expect(response).not_to have_http_status(200)
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
      post "/api/clients/store",
           params: payload.to_json,
           headers: { "Content-Type" => "application/json", "Cookie" => "staff_id=#{staff.id}" }
      expect(response).to have_http_status(201)
      body = JSON.parse(response.body)
      expect(body["id"]).not_to be_nil
    end
  end

  describe "PUT /api/clients/:id/update" do
    it "クライアントを更新して詳細を返す" do
      staff  = create_staff
      client = create_client
      put "/api/clients/#{client.id}/update",
          params: { name: "更新後クライアント名" }.to_json,
          headers: { "Content-Type" => "application/json", "Cookie" => "staff_id=#{staff.id}" }
      expect(response).to have_http_status(200)
      body = JSON.parse(response.body)
      expect(body["name"]).to eq("更新後クライアント名")
    end
  end

  describe "DELETE /api/clients/:id/delete" do
    it "クライアントを削除して200を返す" do
      staff  = create_staff
      client = create_client
      delete "/api/clients/#{client.id}/delete",
             headers: { "Cookie" => "staff_id=#{staff.id}" }
      expect(response).to have_http_status(200)
    end
  end
end
