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

/** PATCH /notifications（一括既読等） */
export async function patchNotificationsBulk(body: unknown): Promise<unknown> {
  return apiPatch("/notifications", body);
}

/** PATCH /notifications/{id} */
export async function patchNotification(id: string, body: unknown): Promise<unknown> {
  return apiPatch(`/notifications/${encodeURIComponent(id)}`, body);
}
