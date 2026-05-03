/**
 * 通知リポジトリ Drizzle 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { and, count, desc, eq, lt, or } from "drizzle-orm";
import { notifications } from "../model/schema.js";
import type { NotificationRepository } from "../../domain/notification/repository.js";
import type { Notification } from "../../domain/notification/entity.js";
import type { NotificationCounts } from "../../domain/notification/valueObjects.js";
import type { DB } from "../../db/client.js";
import { conflict } from "../../lib/errors.js";

function encodeCursor(ts: number, id: number): string {
  return Buffer.from(`${ts},${id}`).toString("base64");
}

function decodeCursor(cursor: string): { ts: number; id: number } {
  const [ts, id] = Buffer.from(cursor, "base64").toString().split(",");
  return { ts: parseInt(ts!), id: parseInt(id!) };
}

export class DrizzleNotificationRepository implements NotificationRepository {
  constructor(private readonly db: DB) {}

  async listPage(staffId: number, cursor: string | undefined, limit: number): Promise<{ items: Notification[]; nextCursor: string | null }> {
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

    const rows = await this.db.select().from(notifications)
      .where(cond)
      .orderBy(desc(notifications.createdAt), desc(notifications.id))
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

  async count(staffId: number): Promise<NotificationCounts> {
    const [totalRow] = await this.db.select({ cnt: count() }).from(notifications).where(eq(notifications.staffId, staffId));
    const [unreadRow] = await this.db.select({ cnt: count() }).from(notifications).where(and(eq(notifications.staffId, staffId), eq(notifications.read, false)));
    return { total: totalRow?.cnt ?? 0, unread: unreadRow?.cnt ?? 0 };
  }

  async bulkMarkRead(executorId: number, ids: number[], allFlag: boolean): Promise<number> {
    const cond = and(eq(notifications.staffId, executorId), eq(notifications.read, false))!;
    const targets = await this.db.select().from(notifications).where(cond);
    const filtered = allFlag ? targets : targets.filter(n => ids.includes(n.id));
    const updated = filtered.length;
    if (updated > 0) {
      for (const n of filtered) {
        const [result] = await this.db.update(notifications)
          .set({ read: true, version: n.version + 1, updatedAt: new Date() })
          .where(and(eq(notifications.id, n.id), eq(notifications.version, n.version)));
        if (result.affectedRows === 0) throw conflict("optimistic_lock_conflict");
      }
    }
    return updated;
  }

  async insert(data: typeof notifications.$inferInsert): Promise<void> {
    await this.db.insert(notifications).values(data);
  }

  async findById(id: number): Promise<Notification | undefined> {
    const rows = await this.db.select().from(notifications).where(eq(notifications.id, id)).limit(1);
    return rows[0];
  }

  async patch(id: number, data: Partial<Pick<Notification, "read" | "title" | "message">>, version: number): Promise<void> {
    const [result] = await this.db.update(notifications)
      .set({ ...data, version: version + 1, updatedAt: new Date() })
      .where(and(eq(notifications.id, id), eq(notifications.version, version)));
    if (result.affectedRows === 0) throw conflict("optimistic_lock_conflict");
  }
}
