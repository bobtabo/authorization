require "spec_helper"

RSpec.describe "Staffs" do
  let(:container) { stub_container }
  let(:staff)     { staff_fixture }

  describe "GET /api/staffs" do
    it "スタッフ一覧を返す" do
      allow(container[:staff_uc]).to receive(:find_by_condition).and_return([staff])
      get "/api/staffs"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["items"]).to be_an(Array)
      expect(body["items"].first["id"]).to eq(1)
    end
  end

  describe "PATCH /api/staffs/:id/updateRole" do
    it "ロールを更新して id を返す" do
      allow(container[:staff_uc]).to receive(:update_role)
      patch "/api/staffs/1/updateRole", { role: 2 }.to_json,
            { "CONTENT_TYPE" => "application/json", "HTTP_COOKIE" => "staff_id=1" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(1)
    end
  end

  describe "PATCH /api/staffs/:id/restore" do
    it "スタッフを復元して id を返す" do
      allow(container[:staff_uc]).to receive(:restore)
      patch "/api/staffs/1/restore"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(1)
    end
  end

  describe "DELETE /api/staffs/:id/delete" do
    it "スタッフを削除して id を返す" do
      allow(container[:staff_uc]).to receive(:destroy)
      delete "/api/staffs/1/delete", {}, { "HTTP_COOKIE" => "staff_id=1" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(1)
    end
  end
end
