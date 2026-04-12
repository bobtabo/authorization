import { upsertStaff, findStaffById } from "../repositories/staffRepo.js";
import type { Staff } from "../db/schema.js";

export async function findUser(staffId: number): Promise<Staff | undefined> {
  return findStaffById(staffId);
}

export async function login(
  provider: string, providerId: string,
  name: string, email: string, avatar?: string,
): Promise<Staff> {
  return upsertStaff({ provider, providerId, name, email, avatar, role: 0 });
}
