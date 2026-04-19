import { describe, test, expect } from "vitest";
import { createApp } from "../app.js";
import { makeInvitation } from "./helpers.js";

const app = createApp();

describe("Invitations", () => {
  describe("GET /api/invitation", () => {
    test("現在の招待URLが取得できる", async () => {
      const inv = await makeInvitation("current-token");
      const res = await app.request("/api/invitation");
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.token).toBe(inv.token);
    });
  });
});
