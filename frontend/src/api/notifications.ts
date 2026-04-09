import { apiGet, apiPatch, apiPost } from "./http";

function executorHeader(executorId?: number | null): Record<string, string> {
  return executorId != null ? { "X-Executor-Id": String(executorId) } : {};
}

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
export async function readAllNotifications(
  body: unknown,
  executorId?: number | null,
): Promise<unknown> {
  return apiPatch("/notifications", body, { headers: executorHeader(executorId) });
}

/** PATCH /notifications/{id}（既読） */
export async function readNotification(
  id: number,
  body: unknown,
  executorId?: number | null,
): Promise<unknown> {
  return apiPatch(`/notifications/${id}`, body, { headers: executorHeader(executorId) });
}
