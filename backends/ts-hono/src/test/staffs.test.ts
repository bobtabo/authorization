import { describe, test, expect } from "vitest";
import { createApp } from "../app.js";
import { makeStaff, signStaffCookie, softDeleteStaff } from "./helpers.js";

const app = createApp();

describe("Staffs", () => {
  describe("GET /api/staffs", () => {
    test("スタッフ一覧が取得できる", async () => {
      await makeStaff({ email: "s1@example.com" });
      await makeStaff({ email: "s2@example.com", name: "別スタッフ" });
      const res = await app.request("/api/staffs");
      expect(res.status).toBe(200);
      const body = await res.json() as { data: unknown[]; pager: unknown };
      expect(body.data.length).toBe(2);
      expect(body.pager).toBeDefined();
    });

    test("スタッフが存在しない場合空リストを返す", async () => {
      const res = await app.request("/api/staffs");
      expect(res.status).toBe(200);
      const body = await res.json() as { data: unknown[] };
      expect(body.data).toEqual([]);
    });

    test("keywordの_はワイルドカードとして解釈されない", async () => {
      await makeStaff({ name: "アンダースコア", email: "a_b@example.com" });
      await makeStaff({ name: "エックス", email: "axb@example.com" });
      const res = await app.request(`/api/staffs?keyword=${encodeURIComponent("a_b")}`);
      expect(res.status).toBe(200);
      const body = await res.json() as { data: unknown[] };
      expect(body.data.length).toBe(1);
    });
  });

  describe("PATCH /api/staffs/:id/updateRole", () => {
    test("ロールが更新できる", async () => {
      const staff = await makeStaff({ role: 2 });
      const executor = await makeStaff({ email: "exec@example.com" });
      const res = await app.request(`/api/staffs/${staff.id}/updateRole`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Cookie: `staff_id=${signStaffCookie(executor.id)}`,
        },
        body: JSON.stringify({ role: 1, version: 1 }),
      });
      expect(res.status).toBe(200);
    });

    test("未認証の場合401が返る", async () => {
      const staff = await makeStaff({ role: 2 });
      const res = await app.request(`/api/staffs/${staff.id}/updateRole`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role: 1, version: 1 }),
      });
      expect(res.status).toBe(401);
    });

    test("実行者がAdmin以外の場合403が返る", async () => {
      const staff = await makeStaff({ role: 2 });
      const executor = await makeStaff({ email: "exec-non-admin@example.com", role: 2 });
      const res = await app.request(`/api/staffs/${staff.id}/updateRole`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Cookie: `staff_id=${signStaffCookie(executor.id)}`,
        },
        body: JSON.stringify({ role: 1, version: 1 }),
      });
      expect(res.status).toBe(403);
    });

    test("実行者が無効化済みAdminの場合403が返る", async () => {
      const staff = await makeStaff({ role: 2 });
      const executor = await makeStaff({ email: "exec-deleted-admin@example.com" });
      await softDeleteStaff(executor.id);
      const res = await app.request(`/api/staffs/${staff.id}/updateRole`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Cookie: `staff_id=${signStaffCookie(executor.id)}`,
        },
        body: JSON.stringify({ role: 1, version: 1 }),
      });
      expect(res.status).toBe(403);
    });
  });

  describe("DELETE /api/staffs/:id/delete", () => {
    test("スタッフが削除できる", async () => {
      const staff = await makeStaff({ email: "del@example.com" });
      const executor = await makeStaff({ email: "exec2@example.com" });
      const res = await app.request(`/api/staffs/${staff.id}/delete`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json", Cookie: `staff_id=${signStaffCookie(executor.id)}` },
        body: JSON.stringify({ version: 1 }),
      });
      expect(res.status).toBe(200);
    });

    test("存在しないIDで404が返る", async () => {
      const executor = await makeStaff({ email: "exec3@example.com" });
      const res = await app.request("/api/staffs/99999/delete", {
        method: "DELETE",
        headers: { "Content-Type": "application/json", Cookie: `staff_id=${signStaffCookie(executor.id)}` },
        body: JSON.stringify({ version: 1 }),
      });
      expect(res.status).toBe(404);
    });
  });
});
