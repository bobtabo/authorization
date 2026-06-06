/**
 * スタッフルーターモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { Hono } from "hono";
import { badRequest } from "../lib/errors.js";
import { formatTime, getStaffIdFromCookie } from "../lib/cookie.js";
import { buildPager } from "../lib/pager.js";
import { db, asTx } from "../db/client.js";
import { DrizzleStaffRepository } from "../infrastructure/persistence/drizzleStaffRepository.js";
import { StaffInteractor } from "../usecase/staff/interactor.js";
import type { StaffListItem } from "../domain/staff/valueObjects.js";

const app = new Hono();

function mapStaff(s: StaffListItem) {
  return {
    id: s.id, name: s.name, email: s.email, role: s.role, status: s.status,
    created_at: formatTime(s.createdAt), updated_at: formatTime(s.updatedAt),
  };
}

app.get("/staffs", async (c) => {
  const keyword = c.req.query("keyword");
  const rolesRaw = c.req.queries("roles") ?? [];
  const roles = rolesRaw.flatMap(r => r.split(",")).map(Number).filter(n => !isNaN(n));
  const limit = Math.max(1, parseInt(c.req.query("limit") ?? "20", 10) || 20);
  const page = Math.max(1, parseInt(c.req.query("page") ?? "1", 10) || 1);
  const offset = limit * (page - 1);
  const sort = c.req.query("sort");
  const sortType = c.req.query("sort_type");

  const uc = new StaffInteractor(new DrizzleStaffRepository(db));
  const [list, count] = await uc.findByCondition(keyword, roles, offset, limit, sort, sortType);
  const pager = buildPager(count, limit, offset, list.length);
  return c.json({ data: list.map(mapStaff), pager });
});

app.patch("/staffs/:id/updateRole", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  const body = await c.req.json<{ role?: number }>();
  if (body.role === undefined) throw badRequest("role_required");
  const executorId = getStaffIdFromCookie(c);
  await db.transaction(async (tx) => {
    const uc = new StaffInteractor(new DrizzleStaffRepository(asTx(tx)));
    await uc.updateRole(id, body.role!, executorId);
  });
  return c.json({ id });
});

app.patch("/staffs/:id/restore", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  await db.transaction(async (tx) => {
    const uc = new StaffInteractor(new DrizzleStaffRepository(asTx(tx)));
    await uc.restore(id);
  });
  return c.json({ id });
});

app.delete("/staffs/:id/delete", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  const executorId = getStaffIdFromCookie(c);
  await db.transaction(async (tx) => {
    const uc = new StaffInteractor(new DrizzleStaffRepository(asTx(tx)));
    await uc.destroy(id, executorId);
  });
  return c.json({ id });
});

export default app;
