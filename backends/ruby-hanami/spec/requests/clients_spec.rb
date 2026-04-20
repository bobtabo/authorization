require "spec_helper"

RSpec.describe "Clients" do
  let(:container) { stub_container }
  let(:client)    { client_fixture }

  describe "GET /api/clients" do
    it "クライアント一覧を返す" do
      allow(container[:client_uc]).to receive(:find_by_condition).and_return([client])
      get "/api/clients"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body).to be_an(Array)
      expect(body.first["id"]).to eq(1)
    end
  end

  describe "GET /api/clients/:id" do
    it "クライアント詳細を返す" do
      allow(container[:client_uc]).to receive(:find_by_id).with(1).and_return(client)
      get "/api/clients/1"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["name"]).to eq(client.name)
      expect(body["identifier"]).to eq(client.identifier)
    end
  end

  describe "POST /api/clients/store" do
    let(:payload) do
      { name: "新規クライアント", post_code: "100-0001", pref: "東京都",
        city: "千代田区", address: "千代田1-1", tel: "0312345678", email: "new@example.com" }
    end

    it "クライアントを登録して201を返す" do
      allow(container[:client_uc]).to receive(:store).and_return(client)
      allow(container[:notification_uc]).to receive(:fan_out)
      post "/api/clients/store", payload.to_json, { "CONTENT_TYPE" => "application/json" }
      expect(last_response.status).to eq(201)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(1)
    end

    it "fan_outが呼び出される" do
      allow(container[:client_uc]).to receive(:store).and_return(client)
      expect(container[:notification_uc]).to receive(:fan_out).once
      post "/api/clients/store", payload.to_json, { "CONTENT_TYPE" => "application/json" }
    end
  end

  describe "PUT /api/clients/:id/update" do
    it "クライアントを更新して詳細を返す" do
      updated = client_fixture(name: "更新後クライアント名")
      allow(container[:client_uc]).to receive(:update).and_return(updated)
      put "/api/clients/1/update", { name: "更新後クライアント名" }.to_json,
          { "CONTENT_TYPE" => "application/json" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["name"]).to eq("更新後クライアント名")
    end
  end

  describe "DELETE /api/clients/:id/delete" do
    it "クライアントを削除して200を返す" do
      allow(container[:client_uc]).to receive(:destroy)
      delete "/api/clients/1/delete"
      expect(last_response.status).to eq(200)
    end
  end
end
