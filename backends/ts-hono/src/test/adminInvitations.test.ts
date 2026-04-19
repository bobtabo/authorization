import { describe, test, expect } from "vitest";
import { createApp } from "../app.js";
import { makeInvitation } from "./helpers.js";

const app = createApp();

describe("AdminInvitations", () => {
  describe("GET /api/admin/invitation/issue", () => {
    test("招待URLが発行できる", async () => {
      const res = await app.request("/api/admin/invitation/issue");
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.token).toBeTruthy();
      expect(body.url).toBeTruthy();
    });

    test("再発行で新しいトークンが返る", async () => {
      await makeInvitation("old-token");
      const res = await app.request("/api/admin/invitation/issue");
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.token).not.toBe("old-token");
    });
  });
});
