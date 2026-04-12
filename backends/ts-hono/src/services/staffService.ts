import { badRequest, notFound } from "../lib/errors.js";
import {
  findAllStaffs, findStaffById, findStaffByIdUnscoped,
  updateStaffRole, softDeleteStaff, restoreStaff,
} from "../repositories/staffRepo.js";
import type { Staff } from "../db/schema.js";

export function staffStatus(staff: Staff): number {
  return staff.deletedAt ? 0 : 1;
}

export async function findByCondition(keyword?: string, roles?: number[]): Promise<Staff[]> {
  return findAllStaffs(keyword, roles);
}

export async function updateRole(staffId: number, role: number, executorId: number): Promise<void> {
  if (staffId === executorId) throw badRequest("cannot_update_own_role");
  const staff = await findStaffById(staffId);
  if (!staff) throw notFound("staff_not_found");
  await updateStaffRole(staffId, role);
}

export async function restore(staffId: number): Promise<void> {
  const staff = await findStaffByIdUnscoped(staffId);
  if (!staff) throw notFound("staff_not_found");
  await restoreStaff(staffId);
}

export async function destroy(staffId: number, executorId: number): Promise<void> {
  if (staffId === executorId) throw badRequest("cannot_delete_self");
  const staff = await findStaffById(staffId);
  if (!staff) throw notFound("staff_not_found");
  await softDeleteStaff(staffId);
}
