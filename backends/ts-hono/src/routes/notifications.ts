import { Hono } from "hono";
import { unauthorized, badRequest } from "../lib/errors.js";
import { getStaffIdFromCookie } from "../lib/cookie.js";
import { config } from "../config.js";
import {
  listPage, countNotifications, bulkRead, patch, mapNotification,
} from "../services/notificationService.js";

const app = new Hono();

app.get("/notifications/counts", async (c) => {
  const staffId = getStaffIdFromCookie(c);
  if (!staffId) throw unauthorized("unauthenticated");
  const { unread, total } = await countNotifications(staffId);
  return c.json({ unread, total });
});

app.get("/notifications", async (c) => {
  const staffId = getStaffIdFromCookie(c);
  if (!staffId) throw unauthorized("unauthenticated");
  const cursor = c.req.query("cursor");
  const limitStr = c.req.query("limit");
  const limit = limitStr ? Math.max(1, parseInt(limitStr, 10)) : config.app.notificationDefaultLimit;
  const page = await listPage(staffId, cursor, limit);
  return c.json({ items: page.items.map(mapNotification), next_cursor: page.nextCursor });
});

app.post("/notifications", async (c) => {
  const body = await c.req.json().catch(() => ({}));
  return c.json({ message: "notification_accepted", received: body }, 202);
});

app.patch("/notifications", async (c) => {
  const body = await c.req.json<{ ids?: number[]; all?: boolean; executor_id?: number }>();
  const executorId = body.executor_id ?? 0;
  if (!executorId) throw unauthorized("unauthenticated");
  if ((!body.ids || body.ids.length === 0) && !body.all) throw badRequest("ids_or_all_required");
  const updated = await bulkRead(executorId, body.ids ?? [], body.all ?? false);
  return c.json({ updated });
});

app.patch("/notifications/:id", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  if (!id || id <= 0) throw badRequest("invalid_id");
  const body = await c.req.json<Record<string, unknown>>();
  await patch(id, {
    read: typeof body.read === "boolean" ? body.read : undefined,
    title: typeof body.title === "string" ? body.title : undefined,
    message: typeof body.message === "string" ? body.message : undefined,
  });
  return c.json({ id });
});

export default app;
