import { apiDelete, apiGet, apiPatch } from "./http";

export type StaffsQuery = {
  keyword?: string;
  roles?: number[];
  statuses?: number[];
};

function executorHeader(executorId?: number | null): Record<string, string> {
  return executorId != null ? { "X-Executor-Id": String(executorId) } : {};
}

/** GET /staffs */
export async function getStaffs(params?: StaffsQuery): Promise<unknown> {
  return apiGet("/staffs", { params });
}

/** PATCH /staffs/{id}/updateRole */
export async function updateStaffRole(
  id: number | string,
  body: { role: number },
  executorId?: number | null,
): Promise<unknown> {
  return apiPatch(`/staffs/${id}/updateRole`, body, { headers: executorHeader(executorId) });
}

/** PATCH /staffs/{id}/restore */
export async function restoreStaff(
  id: number | string,
  executorId?: number | null,
): Promise<unknown> {
  return apiPatch(`/staffs/${id}/restore`, {}, { headers: executorHeader(executorId) });
}

/** DELETE /staffs/{id}/delete */
export async function deleteStaff(
  id: number | string,
  executorId?: number | null,
): Promise<unknown> {
  return apiDelete(`/staffs/${id}/delete`, { headers: executorHeader(executorId) });
}
