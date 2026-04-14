import { notFound, unauthorized, badRequest } from "../lib/errors.js";
import {
  listPage, countNotifications, bulkMarkRead,
  insertNotification, findNotificationById, patchNotification,
} from "../repositories/notificationRepo.js";
import { findAllActiveStaffs } from "../repositories/staffRepo.js";
import type { Notification } from "../db/schema.js";
import { formatTime } from "../lib/cookie.js";

export function mapNotification(n: Notification): Record<string, unknown> {
  return {
    id: n.id,
    staff_id: n.staffId,
    message_type: n.messageType,
    title: n.title,
    message: n.message,
    url: n.url ?? null,
    read: n.read,
    created_at: formatTime(n.createdAt),
    updated_at: formatTime(n.updatedAt),
  };
}

export { listPage, countNotifications };

export async function bulkRead(executorId: number, ids: number[], allFlag: boolean): Promise<number> {
  if (executorId === 0) throw unauthorized("unauthenticated");
  if (ids.length === 0 && !allFlag) throw badRequest("ids_or_all_required");
  return bulkMarkRead(executorId, ids, allFlag);
}

export async function fanOut(title: string, body?: string): Promise<void> {
  const staffs = await findAllActiveStaffs();
  for (const staff of staffs) {
    await insertNotification({ staffId: staff.id, title, message: body ?? "", read: false });
  }
}

export async function patch(id: number, data: Partial<Pick<Notification, "read" | "title" | "message">>): Promise<void> {
  const n = await findNotificationById(id);
  if (!n) throw notFound("notification_not_found");
  await patchNotification(id, data);
}
