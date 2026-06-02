import { describe, test, expect } from "vitest";
import { createApp } from "../app.js";
import { makeClientRecord, makeStaff } from "./helpers.js";

const app = createApp();

describe("Clients", () => {
  describe("GET /api/clients", () => {
    test("クライアント一覧が取得できる", async () => {
      await makeClientRecord({ identifier: "c-001" });
      await makeClientRecord({ identifier: "c-002", email: "c2@example.com" });
      const res = await app.request("/api/clients");
      expect(res.status).toBe(200);
      const body = await res.json() as unknown[];
      expect(body.length).toBe(2);
    });

    test("クライアントが存在しない場合空リストを返す", async () => {
      const res = await app.request("/api/clients");
      expect(res.status).toBe(200);
      expect(await res.json()).toEqual([]);
    });
  });

  describe("GET /api/clients/:id", () => {
    test("クライアント詳細が取得できる", async () => {
      const c = await makeClientRecord();
      const res = await app.request(`/api/clients/${c.id}`);
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.name).toBe(c.name);
    });

    test("存在しないIDで404が返る", async () => {
      const res = await app.request("/api/clients/99999");
      expect(res.status).toBe(404);
    });
  });

  describe("POST /api/clients/store", () => {
    test("クライアントが登録できる", async () => {
      const res = await app.request("/api/clients/store", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: "新規テスト株式会社",
          post_code: "100-0001",
          pref: "東京都",
          city: "千代田区",
          address: "千代田1-1",
          tel: "0312345678",
          email: "new@example.com",
        }),
      });
      expect(res.status).toBe(201);
    });

    test("name未指定で422が返る", async () => {
      const res = await app.request("/api/clients/store", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({}),
      });
      expect(res.status).toBe(422);
    });
  });

  describe("PUT /api/clients/:id/update", () => {
    test("クライアントが更新できる", async () => {
      const c = await makeClientRecord();
      const staff = await makeStaff();
      const res = await app.request(`/api/clients/${c.id}/update`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Cookie: `staff_id=${staff.id}`,
        },
        body: JSON.stringify({ name: "更新後クライアント名" }),
      });
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.name).toBe("更新後クライアント名");
    });
  });

  describe("DELETE /api/clients/:id/delete", () => {
    test("クライアントが削除できる", async () => {
      const c = await makeClientRecord();
      const res = await app.request(`/api/clients/${c.id}/delete`, {
        method: "DELETE",
      });
      expect(res.status).toBe(200);
    });

    test("存在しないIDで404が返る", async () => {
      const res = await app.request("/api/clients/99999/delete", {
        method: "DELETE",
      });
      expect(res.status).toBe(404);
    });
  });

  describe("GET /api/clients/:identifier/qr", () => {
    test("QRコードデータが取得できる", async () => {
      const c = await makeClientRecord({ identifier: "qr-test-001" });
      const res = await app.request(`/api/clients/${c.identifier}/qr`);
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.identifier).toBe(c.identifier);
      expect(body.deeplink_url).toBe(`authgateway://clients/${c.identifier}/info`);
    });

    test("存在しない identifier で404が返る", async () => {
      const res = await app.request("/api/clients/not-exist-ident/qr");
      expect(res.status).toBe(404);
    });
  });

  describe("GET /api/clients/:identifier/info", () => {
    test("クライアント情報が取得できる", async () => {
      const c = await makeClientRecord({ identifier: "info-test-001", name: "情報テスト株式会社", status: 2 });
      const res = await app.request(`/api/clients/${c.identifier}/info`);
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.identifier).toBe(c.identifier);
      expect(body.name).toBe(c.name);
      expect(body.status).toBe(2);
    });

    test("存在しない identifier で404が返る", async () => {
      const res = await app.request("/api/clients/not-exist-ident/info");
      expect(res.status).toBe(404);
    });
  });

  describe("PATCH /api/clients/:identifier/start", () => {
    test("Active 以外のクライアントを Active に変更しアクセストークンを返す", async () => {
      const c = await makeClientRecord({ identifier: "start-test-001", status: 1 });
      const res = await app.request(`/api/clients/${c.identifier}/start`, { method: "PATCH" });
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(typeof body.access_token).toBe("string");
      expect((body.access_token as string).length).toBeGreaterThan(0);
    });

    test("既に Active のクライアントもアクセストークンを返す", async () => {
      const c = await makeClientRecord({ identifier: "start-test-002", status: 2 });
      const res = await app.request(`/api/clients/${c.identifier}/start`, { method: "PATCH" });
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(typeof body.access_token).toBe("string");
    });

    test("存在しない identifier で404が返る", async () => {
      const res = await app.request("/api/clients/not-exist-ident/start", { method: "PATCH" });
      expect(res.status).toBe(404);
    });
  });

  describe("PATCH /api/clients/:identifier/stop", () => {
    test("Active のクライアントを Suspended に変更する", async () => {
      const c = await makeClientRecord({ identifier: "stop-test-001", status: 2 });
      const res = await app.request(`/api/clients/${c.identifier}/stop`, { method: "PATCH" });
      expect(res.status).toBe(200);
      expect(await res.json()).toEqual({});
    });

    test("Active 以外は何もせず200を返す", async () => {
      const c = await makeClientRecord({ identifier: "stop-test-002", status: 1 });
      const res = await app.request(`/api/clients/${c.identifier}/stop`, { method: "PATCH" });
      expect(res.status).toBe(200);
      expect(await res.json()).toEqual({});
    });

    test("存在しない identifier で404が返る", async () => {
      const res = await app.request("/api/clients/not-exist-ident/stop", { method: "PATCH" });
      expect(res.status).toBe(404);
    });
  });

  describe("論理削除済みレコードの可視性", () => {
    test("論理削除済みのクライアントが一覧に含まれる", async () => {
      const c = await makeClientRecord({ identifier: "soft-del-001" });
      await app.request(`/api/clients/${c.id}/delete`, { method: "DELETE" });
      const res = await app.request("/api/clients");
      expect(res.status).toBe(200);
      const body = await res.json() as unknown[];
      expect(body.length).toBe(1);
    });

    test("論理削除済みのクライアント詳細が取得できる", async () => {
      const c = await makeClientRecord({ identifier: "soft-del-002" });
      await app.request(`/api/clients/${c.id}/delete`, { method: "DELETE" });
      const res = await app.request(`/api/clients/${c.id}`);
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.id).toBe(c.id);
    });
  });
});
