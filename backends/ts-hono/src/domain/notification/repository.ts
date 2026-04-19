import type { Notification } from "./entity.js";
import type { NotificationPage, NotificationCounts } from "./valueObjects.js";

export interface NotificationRepository {
  listPage(staffId: number, cursor: string | undefined, limit: number): Promise<NotificationPage>;
  count(staffId: number): Promise<NotificationCounts>;
  bulkMarkRead(executorId: number, ids: number[], allFlag: boolean): Promise<number>;
  insert(data: Omit<Notification, "id" | "createdAt" | "updatedAt" | "createdBy" | "updatedBy" | "version">): Promise<void>;
  findById(id: number): Promise<Notification | undefined>;
  patch(id: number, data: Partial<Pick<Notification, "read" | "title" | "message">>): Promise<void>;
}
