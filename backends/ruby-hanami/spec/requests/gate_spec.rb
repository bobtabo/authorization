require "spec_helper"

RSpec.describe "Gate" do
  let(:container) { stub_container }

  describe "GET /api/gate/issue" do
    context "member パラメータあり" do
      it "JWT トークンを返す" do
        allow(container[:gate_uc]).to receive(:issue_token).and_return("jwt.token.here")
        get "/api/gate/issue?member=user-001",
            {}, { "HTTP_AUTHORIZATION" => "Bearer access-token-abc" }
        expect(last_response.status).to eq(200)
        body = JSON.parse(last_response.body)
        expect(body["token"]).to eq("jwt.token.here")
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
    context "token パラメータあり" do
      it "ペイロードを返す" do
        payload = { "sub" => "user-001", "iss" => "authorization" }
        allow(container[:gate_uc]).to receive(:verify).and_return(payload)
        get "/api/gate/client/test-client/verify?token=jwt.token.here"
        expect(last_response.status).to eq(200)
        body = JSON.parse(last_response.body)
        expect(body["sub"]).to eq("user-001")
      end
    end

    context "token パラメータなし" do
      it "400を返す" do
        get "/api/gate/client/test-client/verify"
        expect(last_response.status).to eq(400)
        body = JSON.parse(last_response.body)
        expect(body["error"]).to eq("token_required")
      end
    end
  end
end
