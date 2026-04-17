import { apiGet, apiPatch, apiPost } from "./http";

/** GET /notifications */
export async function getNotifications(params?: {
  cursor?: string;
  limit?: number;
}): Promise<unknown> {
  return apiGet("/notifications", { params });
}

/** GET /notifications/counts */
export async function getNotificationCounts(): Promise<unknown> {
  return apiGet("/notifications/counts");
}

/** POST /notifications（通知トリガー） */
export async function postNotificationTrigger(body: unknown): Promise<unknown> {
  return apiPost("/notifications", body);
}

/** PATCH /notifications（全既読） */
export async function readAllNotifications(): Promise<unknown> {
  return apiPatch("/notifications", undefined);
}

/** PATCH /notifications/{id}（既読） */
export async function readNotification(id: number): Promise<unknown> {
  return apiPatch(`/notifications/${id}`, undefined);
}
