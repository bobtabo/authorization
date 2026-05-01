# frozen_string_literal: true

require "spec_helper"

RSpec.describe "Notifications" do
  before { truncate_tables }

  describe "GET /api/notifications/counts" do
    it "認証済みで未読数と総数を返す" do
      staff = create_staff
      create_notification(staff[:id], "通知1")
      create_notification(staff[:id], "通知2", read: true)
      get "/api/notifications/counts", {}, { "HTTP_COOKIE" => "staff_id=#{staff[:id]}" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["total"]).to eq(2)
      expect(body["unread"]).to eq(1)
    end

    it "未認証で401を返す" do
      get "/api/notifications/counts"
      expect(last_response.status).to eq(401)
    end
  end

  describe "GET /api/notifications" do
    it "認証済みで通知一覧を返す" do
      staff = create_staff
      create_notification(staff[:id], "通知1")
      create_notification(staff[:id], "通知2")
      get "/api/notifications", {}, { "HTTP_COOKIE" => "staff_id=#{staff[:id]}" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["items"]).to be_an(Array)
      expect(body["items"].size).to eq(2)
    end

    it "未認証で401を返す" do
      get "/api/notifications"
      expect(last_response.status).to eq(401)
    end
  end

  describe "PATCH /api/notifications" do
    it "認証済みで一括既読して updated 件数を返す" do
      staff = create_staff
      create_notification(staff[:id], "通知1")
      create_notification(staff[:id], "通知2")
      patch "/api/notifications", {}, { "HTTP_COOKIE" => "staff_id=#{staff[:id]}" }
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["updated"]).to eq(2)
    end

    it "未認証で401を返す" do
      patch "/api/notifications"
      expect(last_response.status).to eq(401)
    end
  end

  describe "PATCH /api/notifications/:id" do
    it "既読にして id を返す" do
      staff = create_staff
      notif = create_notification(staff[:id], "通知1")
      patch "/api/notifications/#{notif[:id]}"
      expect(last_response.status).to eq(200)
      body = JSON.parse(last_response.body)
      expect(body["id"]).to eq(notif[:id])
    end
  end
end
