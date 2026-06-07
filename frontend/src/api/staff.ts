import { apiDelete, apiGet, apiPatch } from "./http";
import type { Pager } from "./clients";

export type StaffsQuery = {
  keyword?: string;
  roles?: number[];
  statuses?: number[];
  page?: number;
  limit?: number;
  sort?: string;
  sort_type?: string;
};

export type StaffApiRow = {
  id: number;
  name: string;
  email: string;
  role: number;
  status: number;
  created_at: string;
  updated_at: string;
  version?: number;
};

function executorHeader(executorId?: number | null): Record<string, string> {
  return executorId != null ? { "X-Executor-Id": String(executorId) } : {};
}

/** GET /staffs */
export async function getStaffs(params?: StaffsQuery): Promise<{ data: StaffApiRow[]; pager: Pager }> {
  return apiGet<{ data: StaffApiRow[]; pager: Pager }>("/staffs", { params });
}

/** PATCH /staffs/{id}/updateRole */
export async function updateStaffRole(
  id: number | string,
  body: { role: number; version: number },
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
  body: { version: number },
  executorId?: number | null,
): Promise<unknown> {
  return apiDelete(`/staffs/${id}/delete`, { data: body, headers: executorHeader(executorId) });
}
