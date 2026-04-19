import { notFound, unauthorized, badRequest } from "../../lib/errors.js";
import { DrizzleNotificationRepository } from "../../infrastructure/persistence/drizzleNotificationRepository.js";
import { DrizzleStaffRepository } from "../../infrastructure/persistence/drizzleStaffRepository.js";
import type { Notification } from "../../domain/notification/entity.js";
import type { NotificationPage, NotificationCounts } from "../../domain/notification/valueObjects.js";
import { formatTime } from "../../lib/cookie.js";

const repo = new DrizzleNotificationRepository();
const staffRepo = new DrizzleStaffRepository();

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

export async function listPage(staffId: number, cursor: string | undefined, limit: number): Promise<NotificationPage> {
  return repo.listPage(staffId, cursor, limit);
}

export async function countNotifications(staffId: number): Promise<NotificationCounts> {
  return repo.count(staffId);
}

export async function bulkRead(executorId: number, ids: number[], allFlag: boolean): Promise<number> {
  if (executorId === 0) throw unauthorized("unauthenticated");
  if (ids.length === 0 && !allFlag) throw badRequest("ids_or_all_required");
  return repo.bulkMarkRead(executorId, ids, allFlag);
}

export async function fanOut(
  title: string,
  body?: string,
  url?: string,
  executorId = 0,
  messageType = 1,
): Promise<void> {
  const staffs = await staffRepo.findAllActive();
  for (const staff of staffs) {
    await repo.insert({
      staffId: staff.id,
      messageType,
      title,
      message: body ?? "",
      url: url ?? null,
      read: false,
      createdBy: executorId,
      updatedBy: executorId,
      version: 1,
    });
  }
}

export async function patch(id: number, data: Partial<Pick<Notification, "read" | "title" | "message">>): Promise<void> {
  const n = await repo.findById(id);
  if (!n) throw notFound("notification_not_found");
  await repo.patch(id, data);
}
