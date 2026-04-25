/**
 * 通知ルーターモジ���ール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { Hono } from "hono";
import { unauthorized, badRequest } from "../lib/errors.js";
import { getStaffIdFromCookie } from "../lib/cookie.js";
import { config } from "../config.js";
import { db, asTx } from "../db/client.js";
import { DrizzleNotificationRepository } from "../infrastructure/persistence/drizzleNotificationRepository.js";
import { DrizzleStaffRepository } from "../infrastructure/persistence/drizzleStaffRepository.js";
import { NotificationInteractor } from "../usecase/notification/interactor.js";

const app = new Hono();

app.get("/notifications/counts", async (c) => {
  const staffId = getStaffIdFromCookie(c);
  if (!staffId) throw unauthorized("unauthenticated");
  const uc = new NotificationInteractor(new DrizzleNotificationRepository(db), new DrizzleStaffRepository(db));
  const { unread, total } = await uc.countNotifications(staffId);
  return c.json({ unread, total });
});

app.get("/notifications", async (c) => {
  const staffId = getStaffIdFromCookie(c);
  if (!staffId) throw unauthorized("unauthenticated");
  const cursor = c.req.query("cursor");
  const limitStr = c.req.query("limit");
  const limit = limitStr ? Math.max(1, parseInt(limitStr, 10)) : config.app.notificationDefaultLimit;
  const uc = new NotificationInteractor(new DrizzleNotificationRepository(db), new DrizzleStaffRepository(db));
  const page = await uc.listPage(staffId, cursor, limit);
  return c.json({
    items: page.items.map(n => ({
      id: n.id, staff_id: n.staffId, message_type: n.messageType,
      title: n.title, message: n.message, url: n.url,
      read: n.read, created_at: n.createdAt, updated_at: n.updatedAt,
    })),
    next_cursor: page.nextCursor,
  });
});

app.patch("/notifications", async (c) => {
  const staffId = getStaffIdFromCookie(c);
  if (!staffId) throw unauthorized("unauthenticated");
  let updated!: number;
  await db.transaction(async (tx) => {
    const uc = new NotificationInteractor(new DrizzleNotificationRepository(asTx(tx)), new DrizzleStaffRepository(asTx(tx)));
    updated = await uc.bulkRead(staffId, [], true);
  });
  return c.json({ updated });
});

app.patch("/notifications/:id", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  if (!id || id <= 0) throw badRequest("invalid_id");
  await db.transaction(async (tx) => {
    const uc = new NotificationInteractor(new DrizzleNotificationRepository(asTx(tx)), new DrizzleStaffRepository(asTx(tx)));
    await uc.patch(id, { read: true });
  });
  return c.json({ id });
});

export default app;
