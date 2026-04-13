import { describe, test, expect } from "vitest";
import { createApp } from "../app.js";
import { makeStaff } from "./helpers.js";

const app = createApp();

describe("Staffs", () => {
  describe("GET /api/staffs", () => {
    test("スタッフ一覧が取得できる", async () => {
      await makeStaff({ email: "s1@example.com" });
      await makeStaff({ email: "s2@example.com", name: "別スタッフ" });
      const res = await app.request("/api/staffs");
      expect(res.status).toBe(200);
      const body = await res.json() as { items: unknown[] };
      expect(body.items.length).toBe(2);
    });

    test("スタッフが存在しない場合空リストを返す", async () => {
      const res = await app.request("/api/staffs");
      expect(res.status).toBe(200);
      const body = await res.json() as { items: unknown[] };
      expect(body.items).toEqual([]);
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
          Cookie: `staff_id=${executor.id}`,
        },
        body: JSON.stringify({ role: 1 }),
      });
      expect(res.status).toBe(200);
    });
  });

  describe("DELETE /api/staffs/:id/delete", () => {
    test("スタッフが削除できる", async () => {
      const staff = await makeStaff({ email: "del@example.com" });
      const executor = await makeStaff({ email: "exec2@example.com" });
      const res = await app.request(`/api/staffs/${staff.id}/delete`, {
        method: "DELETE",
        headers: { Cookie: `staff_id=${executor.id}` },
      });
      expect(res.status).toBe(200);
    });

    test("存在しないIDで404が返る", async () => {
      const executor = await makeStaff({ email: "exec3@example.com" });
      const res = await app.request("/api/staffs/99999/delete", {
        method: "DELETE",
        headers: { Cookie: `staff_id=${executor.id}` },
      });
      expect(res.status).toBe(404);
    });
  });
});
