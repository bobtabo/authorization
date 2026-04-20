require "spec_helper"

RSpec.describe "Notifications" do
  let(:container) { stub_container }

  describe "GET /api/notifications/counts" do
    context "認証済みの場合" do
      it "未読数と総数を返す" do
        allow(container[:notification_uc]).to receive(:counts).with(1).and_return([3, 10])
        get "/api/notifications/counts", {}, { "HTTP_COOKIE" => "staff_id=1" }
        expect(last_response.status).to eq(200)
        body = JSON.parse(last_response.body)
        expect(body["unread"]).to eq(3)
        expect(body["total"]).to eq(10)
      end
    end

    context "未認証の場合" do
      it "401を返す" do
        get "/api/notifications/counts"
        expect(last_response.status).to eq(401)
      end
    end
  end

  describe "GET /api/notifications" do
    context "認証済みの場合" do
      it "通知一覧を返す" do
        notif = notification_fixture
        page  = notification_page_fixture([notif])
        allow(container[:notification_uc]).to receive(:list_page).and_return(page)
        get "/api/notifications", {}, { "HTTP_COOKIE" => "staff_id=1" }
        expect(last_response.status).to eq(200)
        body = JSON.parse(last_response.body)
        expect(body["items"]).to be_an(Array)
        expect(body["items"].first["id"]).to eq(1)
      end
    end

    context "未認証の場合" do
      it "401を返す" do
        get "/api/notifications"
        expect(last_response.status).to eq(401)
      end
    end
  end

  describe "PATCH /api/notifications" do
    context "認証済みの場合" do
      it "一括既読して updated 件数を返す" do
        allow(container[:notification_uc]).to receive(:bulk_mark_read).with(1).and_return(5)
        patch "/api/notifications", {}, { "HTTP_COOKIE" => "staff_id=1" }
        expect(last_response.status).to eq(200)
        body = JSON.parse(last_response.body)
        expect(body["updated"]).to eq(5)
      end
    end

    context "未認証の場合" do
      it "401を返す" do
        patch "/api/notifications"
        expect(last_response.status).to eq(401)
      end
    end
  end

  describe "PATCH /api/notifications/:id" do
    it "既読にして id を返す" do
      allow(container[:notification_uc]).to receive(:mark_read).with(1)
      patch "/api/notifications/1"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(1)
    end
  end
end
