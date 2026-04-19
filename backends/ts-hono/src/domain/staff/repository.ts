import type { Staff } from "./entity.js";

export interface StaffRepository {
  findAll(keyword?: string, roles?: number[]): Promise<Staff[]>;
  findById(id: number): Promise<Staff | undefined>;
  findByIdUnscoped(id: number): Promise<Staff | undefined>;
  findByProvider(provider: number, providerId: string): Promise<Staff | undefined>;
  findAllActive(): Promise<Staff[]>;
  upsert(data: Omit<Staff, "id" | "createdAt" | "updatedAt" | "deletedAt"> & { avatar?: string | null; role?: number | null }): Promise<Staff>;
  updateRole(id: number, role: number): Promise<void>;
  softDelete(id: number): Promise<void>;
  restore(id: number): Promise<void>;
}
