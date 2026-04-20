require "spec_helper"

RSpec.describe "Auth" do
  let(:container) { stub_container }
  let(:staff)     { staff_fixture }

  describe "GET /api/auth/me" do
    context "認証済みの場合" do
      it "スタッフ情報を返す" do
        allow(container[:auth_uc]).to receive(:find_user).with(1).and_return(staff)
        get "/api/auth/me", {}, { "HTTP_COOKIE" => "staff_id=1" }
        expect(last_response.status).to eq(200)
        body = JSON.parse(last_response.body)
        expect(body["staff_id"]).to eq(1)
        expect(body["name"]).to eq(staff.name)
      end
    end

    context "未認証の場合" do
      it "401を返す" do
        get "/api/auth/me"
        expect(last_response.status).to eq(401)
      end
    end
  end

  describe "GET /api/auth/login" do
    context "認証済みの場合" do
      it "ログイン情報を返す" do
        allow(container[:auth_uc]).to receive(:find_user).with(1).and_return(staff)
        get "/api/auth/login", {}, { "HTTP_COOKIE" => "staff_id=1" }
        expect(last_response.status).to eq(200)
        body = JSON.parse(last_response.body)
        expect(body["staff_id"]).to eq(1)
      end
    end

    context "未認証の場合" do
      it "401を返す" do
        get "/api/auth/login"
        expect(last_response.status).to eq(401)
      end
    end
  end

  describe "GET /api/auth/logout" do
    it "200を返す" do
      get "/api/auth/logout"
      expect(last_response.status).to eq(200)
    end
  end

  describe "GET /api/auth/invitation/:token" do
    it "招待情報を返す" do
      inv = invitation_fixture
      allow(container[:invitation_uc]).to receive(:find_by_token).and_return(inv)
      get "/api/auth/invitation/abc123"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["token"]).to eq("abc123")
      expect(body["found"]).to be true
    end
  end
end
