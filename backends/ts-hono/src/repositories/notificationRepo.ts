import { and, count, eq, gt, lt, or } from "drizzle-orm";
import { db } from "../db/client.js";
import { notifications, type Notification } from "../db/schema.js";

export interface NotificationPage {
  items: Notification[];
  nextCursor: string | null;
}

function encodeCursor(ts: number, id: number): string {
  return Buffer.from(`${ts},${id}`).toString("base64");
}

function decodeCursor(cursor: string): { ts: number; id: number } {
  const [ts, id] = Buffer.from(cursor, "base64").toString().split(",");
  return { ts: parseInt(ts!), id: parseInt(id!) };
}

export async function listPage(staffId: number, cursor: string | undefined, limit: number): Promise<NotificationPage> {
  let cond = eq(notifications.staffId, staffId);

  if (cursor) {
    const { ts, id } = decodeCursor(cursor);
    const curDate = new Date(ts * 1000);
    cond = and(
      cond,
      or(
        lt(notifications.createdAt, curDate),
        and(eq(notifications.createdAt, curDate), lt(notifications.id, id)),
      )!,
    )!;
  }

  const rows = await db.select().from(notifications)
    .where(cond)
    .orderBy(notifications.createdAt, notifications.id)
    .limit(limit + 1);

  let nextCursor: string | null = null;
  if (rows.length > limit) {
    rows.splice(limit);
    const last = rows[rows.length - 1]!;
    const ts = Math.floor((last.createdAt?.getTime() ?? 0) / 1000);
    nextCursor = encodeCursor(ts, last.id);
  }

  return { items: rows, nextCursor };
}

export async function countNotifications(staffId: number): Promise<{ unread: number; total: number }> {
  const [totalRow] = await db.select({ cnt: count() }).from(notifications).where(eq(notifications.staffId, staffId));
  const [unreadRow] = await db.select({ cnt: count() }).from(notifications).where(and(eq(notifications.staffId, staffId), eq(notifications.read, false)));
  return { total: totalRow?.cnt ?? 0, unread: unreadRow?.cnt ?? 0 };
}

export async function bulkMarkRead(executorId: number, ids: number[], allFlag: boolean): Promise<number> {
  const cond = allFlag
    ? and(eq(notifications.staffId, executorId), eq(notifications.read, false))!
    : and(eq(notifications.staffId, executorId), eq(notifications.read, false))!;

  const targets = await db.select().from(notifications).where(cond);
  const filtered = allFlag ? targets : targets.filter(n => ids.includes(n.id));
  const updated = filtered.length;
  if (updated > 0) {
    for (const n of filtered) {
      await db.update(notifications).set({ read: true, updatedAt: new Date() }).where(eq(notifications.id, n.id));
    }
  }
  return updated;
}

export async function insertNotification(data: typeof notifications.$inferInsert): Promise<void> {
  await db.insert(notifications).values(data);
}

export async function findNotificationById(id: number): Promise<Notification | undefined> {
  const rows = await db.select().from(notifications).where(eq(notifications.id, id)).limit(1);
  return rows[0];
}

export async function patchNotification(id: number, data: Partial<Pick<Notification, "read" | "title" | "body">>): Promise<void> {
  await db.update(notifications).set({ ...data, updatedAt: new Date() }).where(eq(notifications.id, id));
}
