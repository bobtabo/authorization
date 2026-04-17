import { Hono } from "hono";
import { unauthorized, badRequest } from "../lib/errors.js";
import { getStaffIdFromCookie } from "../lib/cookie.js";
import { config } from "../config.js";
import {
  listPage, countNotifications, bulkRead, patch, mapNotification,
} from "../usecase/notification/interactor.js";

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
  const staffId = getStaffIdFromCookie(c);
  if (!staffId) throw unauthorized("unauthenticated");
  const updated = await bulkRead(staffId, [], true);
  return c.json({ updated });
});

app.patch("/notifications/:id", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  if (!id || id <= 0) throw badRequest("invalid_id");
  await patch(id, { read: true });
  return c.json({ id });
});

export default app;
