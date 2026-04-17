import { DrizzleStaffRepository } from "../../infrastructure/persistence/drizzleStaffRepository.js";
import type { Staff } from "../../domain/staff/entity.js";

const repo = new DrizzleStaffRepository();

export async function findUser(staffId: number): Promise<Staff | undefined> {
  return repo.findById(staffId);
}

export async function login(
  provider: number, providerId: string,
  name: string, email: string, avatar?: string,
): Promise<Staff> {
  return repo.upsert({ provider, providerId, name, email, avatar, role: 0 });
}
