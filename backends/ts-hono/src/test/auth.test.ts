import { describe, test, expect } from "vitest";
import { createApp } from "../app.js";
import { makeStaff, makeInvitation } from "./helpers.js";

const app = createApp();

describe("Auth", () => {
  describe("GET /api/auth/me", () => {
    test("認証済みでプロフィールが取得できる", async () => {
      const staff = await makeStaff();
      const res = await app.request("/api/auth/me", {
        headers: { Cookie: `staff_id=${staff.id}` },
      });
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.staff_id).toBe(staff.id);
      expect(body.name).toBe(staff.name);
    });

    test("未認証で401が返る", async () => {
      const res = await app.request("/api/auth/me");
      expect(res.status).toBe(401);
    });
  });

  describe("GET /api/auth/logout", () => {
    test("ログアウトが成功する", async () => {
      const res = await app.request("/api/auth/logout");
      expect(res.status).toBe(200);
    });
  });

  describe("GET /api/auth/invitation/:token", () => {
    test("有効なトークンで招待情報が取得できる", async () => {
      const inv = await makeInvitation("valid-test-token");
      const res = await app.request(`/api/auth/invitation/${inv.token}`);
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.token).toBe(inv.token);
    });
  });
});
