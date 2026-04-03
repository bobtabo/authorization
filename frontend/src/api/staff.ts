import { apiDelete, apiGet, apiPatch } from "./http";

export type StaffsQuery = {
  keyword?: string;
  roles?: number[];
  statuses?: number[];
};

/** GET /staffs */
export async function getStaffs(params?: StaffsQuery): Promise<unknown> {
  return apiGet("/staffs", { params });
}

/** PATCH /staffs/{id}/updateRole */
export async function updateStaffRole(
  id: number | string,
  body: { role: number },
): Promise<unknown> {
  return apiPatch(`/staffs/${id}/updateRole`, body);
}

/** DELETE /staffs/{id}/delete */
export async function deleteStaff(id: number | string): Promise<unknown> {
  return apiDelete(`/staffs/${id}/delete`);
}
