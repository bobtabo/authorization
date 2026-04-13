import { Hono } from "hono";
import { badRequest } from "../lib/errors.js";
import { formatTime, getStaffIdFromCookie } from "../lib/cookie.js";
import { findByCondition, updateRole, restore, destroy, staffStatus } from "../services/staffService.js";
import type { Staff } from "../db/schema.js";

const app = new Hono();

function mapStaff(s: Staff) {
  return {
    id: s.id, name: s.name, email: s.email, role: s.role,
    status: staffStatus(s),
    created_at: formatTime(s.createdAt), updated_at: formatTime(s.updatedAt),
  };
}

app.get("/staffs", async (c) => {
  const keyword = c.req.query("keyword");
  const rolesRaw = c.req.queries("roles") ?? [];
  const roles = rolesRaw.flatMap(r => r.split(",")).map(Number).filter(n => !isNaN(n));
  const list = await findByCondition(keyword, roles);
  return c.json({ items: list.map(mapStaff) });
});

app.patch("/staffs/:id/updateRole", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  const body = await c.req.json<{ role?: number }>();
  if (body.role === undefined) throw badRequest("role_required");
  const executorId = getStaffIdFromCookie(c);
  await updateRole(id, body.role, executorId);
  return c.json({ id });
});

app.patch("/staffs/:id/restore", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  await restore(id);
  return c.json({ id });
});

app.delete("/staffs/:id/delete", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  const executorId = getStaffIdFromCookie(c);
  await destroy(id, executorId);
  return c.json({ id });
});

export default app;
