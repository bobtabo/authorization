require "spec_helper"

RSpec.describe "Admin::Invitations" do
  let(:container) { stub_container }
  let(:inv)       { invitation_fixture }

  describe "GET /api/admin/invitation" do
    it "現在の招待情報を返す" do
      allow(container[:invitation_uc]).to receive(:current).and_return(inv)
      get "/api/admin/invitation"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["found"]).to be true
      expect(body["token"]).to eq(inv.token)
    end
  end

  describe "GET /api/admin/invitation/issue" do
    it "新しい招待を発行して返す" do
      allow(container[:invitation_uc]).to receive(:issue).and_return(inv)
      get "/api/admin/invitation/issue"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["found"]).to be true
      expect(body["token"]).to eq(inv.token)
    end
  end
end
