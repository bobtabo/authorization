import { describe, test, expect } from "vitest";
import { createApp } from "../app.js";
import { makeStaff, makeInvitation, signStaffCookie } from "./helpers.js";
import { signStaffId } from "../lib/staffSession.js";

const app = createApp();

describe("Auth", () => {
  describe("GET /api/auth/me", () => {
    test("認証済みでプロフィールが取得できる", async () => {
      const staff = await makeStaff();
      const res = await app.request("/api/auth/me", {
        headers: { Cookie: `staff_id=${signStaffCookie(staff.id)}` },
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

    test("署名の無い改ざんクッキーでは401が返る", async () => {
      const staff = await makeStaff();
      const res = await app.request("/api/auth/me", {
        headers: { Cookie: `staff_id=${staff.id}` },
      });
      expect(res.status).toBe(401);
    });

    test("署名部分が改ざんされたクッキーでは401が返る", async () => {
      const staff = await makeStaff();
      const signed = signStaffCookie(staff.id);
      const [idPart, expPart, sig] = signed.split(".");
      const tampered = `${idPart}.${expPart}.${sig.slice(0, -1)}${sig.at(-1) === "0" ? "1" : "0"}`;
      const res = await app.request("/api/auth/me", {
        headers: { Cookie: `staff_id=${tampered}` },
      });
      expect(res.status).toBe(401);
    });

    test("別のシークレットで署名されたクッキーでは401が返る", async () => {
      const staff = await makeStaff();
      const forged = signStaffId(staff.id, "wrong-secret", 3600);
      const res = await app.request("/api/auth/me", {
        headers: { Cookie: `staff_id=${forged}` },
      });
      expect(res.status).toBe(401);
    });

    test("有効期限切れのクッキーでは401が返る", async () => {
      const staff = await makeStaff();
      const secret = process.env.STAFF_COOKIE_SECRET ?? "test-staff-cookie-secret";
      const expired = signStaffId(staff.id, secret, -1);
      const res = await app.request("/api/auth/me", {
        headers: { Cookie: `staff_id=${expired}` },
      });
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

  describe("OAuth state nonce", () => {
    test("認可開始時にnonceクッキーを発行しstateに含める", async () => {
      const res = await app.request("/auth/google/redirect?token=inv-token");
      expect(res.status).toBe(302);
      const setCookie = res.headers.get("set-cookie") ?? "";
      const m = setCookie.match(/oauth_state=([^;]+)/);
      expect(m).not.toBeNull();
      expect(setCookie).toMatch(/HttpOnly/i);
      const nonce = m![1];
      const loc = new URL(res.headers.get("location") ?? "");
      expect(loc.searchParams.get("state")).toBe(`ts|${nonce}|inv-token`);
    });

    test("nonceクッキーが無い場合は400エラーページへリダイレクトする", async () => {
      const res = await app.request("/auth/google/callback?code=x&state=ts%7Cabc");
      expect(res.status).toBe(302);
      expect(res.headers.get("location")).toMatch(/\/error\?code=400$/);
    });

    test("nonceが一致しない場合は400エラーページへリダイレクトする", async () => {
      const res = await app.request("/auth/github/callback?code=x&state=ts%7Cabc", {
        headers: { Cookie: "oauth_state=xyz" },
      });
      expect(res.status).toBe(302);
      expect(res.headers.get("location")).toMatch(/\/error\?code=400$/);
    });

    test("stateにnonceセグメントが無い場合は400エラーページへリダイレクトする", async () => {
      const res = await app.request("/auth/github/callback?code=x&state=ts", {
        headers: { Cookie: "oauth_state=abc" },
      });
      expect(res.status).toBe(302);
      expect(res.headers.get("location")).toMatch(/\/error\?code=400$/);
    });
  });
});
