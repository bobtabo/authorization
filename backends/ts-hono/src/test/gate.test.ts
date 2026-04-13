import { describe, test, expect } from "vitest";
import { createApp } from "../app.js";
import { makeClientRecord } from "./helpers.js";

const app = createApp();

describe("Gate", () => {
  describe("GET /api/gate/issue", () => {
    test("JWTが発行できる", async () => {
      const c = await makeClientRecord();
      const res = await app.request("/api/gate/issue?member=member-001", {
        headers: { Authorization: `Bearer ${c.token}` },
      });
      expect(res.status).toBe(200);
      const body = await res.json() as Record<string, unknown>;
      expect(body.token).toBeTruthy();
    });

    test("memberパラメーター未指定で400が返る", async () => {
      const c = await makeClientRecord({ identifier: `c-no-member-${Date.now()}` });
      const res = await app.request("/api/gate/issue", {
        headers: { Authorization: `Bearer ${c.token}` },
      });
      // member は required Query なので 400
      expect(res.status).toBe(400);
    });

    test("無効なトークンで401が返る", async () => {
      const res = await app.request("/api/gate/issue?member=member-001", {
        headers: { Authorization: "Bearer invalid-token" },
      });
      expect(res.status).toBe(401);
    });
  });

  describe("GET /api/gate/client/:identifier/verify", () => {
    test("JWTが検証できる", async () => {
      const c = await makeClientRecord();
      // JWT を発行
      const issueRes = await app.request("/api/gate/issue?member=member-001", {
        headers: { Authorization: `Bearer ${c.token}` },
      });
      expect(issueRes.status).toBe(200);
      const { token: jwt } = await issueRes.json() as { token: string };

      // JWT を検証
      const res = await app.request(
        `/api/gate/client/${c.identifier}/verify?token=${jwt}`,
      );
      expect(res.status).toBe(200);
    });

    test("存在しないidentifierで401か404が返る", async () => {
      const res = await app.request("/api/gate/client/unknown-client/verify?token=dummy");
      expect([401, 404]).toContain(res.status);
    });

    test("無効なJWTで401が返る", async () => {
      const c = await makeClientRecord({ identifier: `c-invalid-jwt-${Date.now()}` });
      const res = await app.request(
        `/api/gate/client/${c.identifier}/verify?token=invalid.jwt.token`,
      );
      expect(res.status).toBe(401);
    });
  });
});
