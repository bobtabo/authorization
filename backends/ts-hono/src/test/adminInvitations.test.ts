import { describe, test, expect } from "vitest";
import { createApp } from "../app.js";
import { makeInvitation, makeStaff } from "./helpers.js";

const app = createApp();

describe("AdminInvitations", () => {
  describe("GET /api/admin/invitation", () => {
    test("現在の招待URLが取得できる", async () => {
      const inv = await makeInvitation("current-token", 2);
      const res = await app.request("/api/admin/invitation?role=2");
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.token).toBe(inv.token);
    });

    test("roleパラメータが不正な場合は400が返る", async () => {
      const res = await app.request("/api/admin/invitation?role=3");
      expect(res.status).toBe(400);
    });
  });

  describe("GET /api/admin/invitation/issue", () => {
    test("認証済みで招待URLが発行できる", async () => {
      const staff = await makeStaff();
      const res = await app.request("/api/admin/invitation/issue?role=2", {
        headers: { Cookie: `staff_id=${staff.id}` },
      });
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.token).toBeTruthy();
      expect(body.url).toBeTruthy();
    });

    test("未認証で401が返る", async () => {
      const res = await app.request("/api/admin/invitation/issue");
      expect(res.status).toBe(401);
    });

    test("roleパラメータが不正な場合は400が返る", async () => {
      const staff = await makeStaff();
      const res = await app.request("/api/admin/invitation/issue?role=5", {
        headers: { Cookie: `staff_id=${staff.id}` },
      });
      expect(res.status).toBe(400);
    });

    test("再発行で新しいトークンが返る", async () => {
      const staff = await makeStaff();
      await makeInvitation("old-token", 2);
      const res = await app.request("/api/admin/invitation/issue?role=2", {
        headers: { Cookie: `staff_id=${staff.id}` },
      });
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.token).not.toBe("old-token");
    });
  });
});
