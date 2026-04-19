import { badRequest, notFound } from "../../lib/errors.js";
import { DrizzleStaffRepository } from "../../infrastructure/persistence/drizzleStaffRepository.js";
import type { Staff } from "../../domain/staff/entity.js";

const repo = new DrizzleStaffRepository();

export function staffStatus(staff: Staff): number {
  return staff.deletedAt ? 0 : 1;
}

export async function findByCondition(keyword?: string, roles?: number[]): Promise<Staff[]> {
  return repo.findAll(keyword, roles);
}

export async function updateRole(staffId: number, role: number, executorId: number): Promise<void> {
  if (staffId === executorId) throw badRequest("cannot_update_own_role");
  const staff = await repo.findById(staffId);
  if (!staff) throw notFound("staff_not_found");
  await repo.updateRole(staffId, role);
}

export async function restore(staffId: number): Promise<void> {
  const staff = await repo.findByIdUnscoped(staffId);
  if (!staff) throw notFound("staff_not_found");
  await repo.restore(staffId);
}

export async function destroy(staffId: number, executorId: number): Promise<void> {
  if (staffId === executorId) throw badRequest("cannot_delete_self");
  const staff = await repo.findById(staffId);
  if (!staff) throw notFound("staff_not_found");
  await repo.softDelete(staffId);
}
