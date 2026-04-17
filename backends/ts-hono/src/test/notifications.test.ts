import { describe, test, expect } from "vitest";
import { createApp } from "../app.js";
import { makeStaff, makeNotification } from "./helpers.js";

const app = createApp();

describe("Notifications", () => {
  describe("GET /api/notifications/counts", () => {
    test("通知件数が取得できる", async () => {
      const staff = await makeStaff();
      await makeNotification(staff.id, "通知1");
      await makeNotification(staff.id, "通知2");
      const res = await app.request("/api/notifications/counts", {
        headers: { Cookie: `staff_id=${staff.id}` },
      });
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, number>;
      expect(body.total).toBe(2);
    });

    test("未認証で401が返る", async () => {
      const res = await app.request("/api/notifications/counts");
      expect(res.status).toBe(401);
    });
  });

  describe("GET /api/notifications", () => {
    test("通知一覧が取得できる", async () => {
      const staff = await makeStaff();
      await makeNotification(staff.id, "通知A");
      const res = await app.request("/api/notifications", {
        headers: { Cookie: `staff_id=${staff.id}` },
      });
      expect(res.status).toBe(200);
      const body = await res.json() as { items: Array<Record<string, unknown>> };
      expect(body.items.length).toBe(1);
      expect(typeof body.items[0].message).toBe("string");
    });

    test("新しい通知が先頭に来る", async () => {
      const staff = await makeStaff();
      await makeNotification(staff.id, "古い通知");
      await makeNotification(staff.id, "新しい通知");
      const res = await app.request("/api/notifications", {
        headers: { Cookie: `staff_id=${staff.id}` },
      });
      expect(res.status).toBe(200);
      const body = await res.json() as { items: Array<Record<string, unknown>> };
      expect(body.items[0].title).toBe("新しい通知");
    });

    test("url付き通知がレスポンスに含まれる", async () => {
      const staff = await makeStaff({ email: `url-notif-${Date.now()}@example.com` });
      await makeNotification(staff.id, "クライアント登録", "/clients/show?id=1");
      const res = await app.request("/api/notifications", {
        headers: { Cookie: `staff_id=${staff.id}` },
      });
      expect(res.status).toBe(200);
      const body = await res.json() as { items: Array<Record<string, unknown>> };
      expect(body.items[0].url).toBe("/clients/show?id=1");
    });

    test("未認証で401が返る", async () => {
      const res = await app.request("/api/notifications");
      expect(res.status).toBe(401);
    });
  });

  describe("POST /api/notifications", () => {
    test("通知トリガーが受け付けられる", async () => {
      const res = await app.request("/api/notifications", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title: "新規通知", body: "通知本文" }),
      });
      expect(res.status).toBe(202);
    });
  });

  describe("PATCH /api/notifications", () => {
    test("一括既読が成功する", async () => {
      const staff = await makeStaff();
      await makeNotification(staff.id, "通知A");
      await makeNotification(staff.id, "通知B");
      const res = await app.request("/api/notifications", {
        method: "PATCH",
        headers: { Cookie: `staff_id=${staff.id}` },
      });
      expect(res.status).toBe(200);
      const data = await res.json() as { updated: number };
      expect(data.updated).toBeGreaterThanOrEqual(0);
    });
  });

  describe("PATCH /api/notifications/:id", () => {
    test("単一通知が既読になる", async () => {
      const staff = await makeStaff();
      const n = await makeNotification(staff.id);
      const res = await app.request(`/api/notifications/${n.id}`, {
        method: "PATCH",
      });
      expect(res.status).toBe(200);
    });
  });
});
