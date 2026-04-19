import { and, eq, inArray, isNull, like, or } from "drizzle-orm";
import { db } from "../../db/client.js";
import { staffs } from "../model/schema.js";
import type { StaffRepository } from "../../domain/staff/repository.js";
import type { Staff } from "../../domain/staff/entity.js";

export class DrizzleStaffRepository implements StaffRepository {
  async findAll(keyword?: string, roles?: number[]): Promise<Staff[]> {
    const conds = [];
    if (keyword) conds.push(or(like(staffs.name, `%${keyword}%`), like(staffs.email, `%${keyword}%`))!);
    if (roles && roles.length > 0) conds.push(inArray(staffs.role, roles));
    return db.select().from(staffs).where(conds.length ? and(...conds) : undefined).orderBy(staffs.id);
  }

  async findById(id: number): Promise<Staff | undefined> {
    const rows = await db.select().from(staffs).where(and(eq(staffs.id, id), isNull(staffs.deletedAt))).limit(1);
    return rows[0];
  }

  async findByIdUnscoped(id: number): Promise<Staff | undefined> {
    const rows = await db.select().from(staffs).where(eq(staffs.id, id)).limit(1);
    return rows[0];
  }

  async findByProvider(provider: number, providerId: string): Promise<Staff | undefined> {
    const rows = await db.select().from(staffs)
      .where(and(eq(staffs.provider, provider), eq(staffs.providerId, providerId)))
      .limit(1);
    return rows[0];
  }

  async findAllActive(): Promise<Staff[]> {
    return db.select().from(staffs).where(isNull(staffs.deletedAt));
  }

  async upsert(data: typeof staffs.$inferInsert): Promise<Staff> {
    const existing = await this.findByProvider(data.provider, data.providerId);
    if (existing) {
      await db.update(staffs).set({ name: data.name, email: data.email, avatar: data.avatar, updatedAt: new Date() }).where(eq(staffs.id, existing.id));
      return (await db.select().from(staffs).where(eq(staffs.id, existing.id)).limit(1))[0]!;
    }
    await db.insert(staffs).values(data);
    return (await db.select().from(staffs).where(and(eq(staffs.provider, data.provider), eq(staffs.providerId, data.providerId))).limit(1))[0]!;
  }

  async updateRole(id: number, role: number): Promise<void> {
    await db.update(staffs).set({ role, updatedAt: new Date() }).where(eq(staffs.id, id));
  }

  async softDelete(id: number): Promise<void> {
    await db.update(staffs).set({ deletedAt: new Date(), updatedAt: new Date() }).where(eq(staffs.id, id));
  }

  async restore(id: number): Promise<void> {
    await db.update(staffs).set({ deletedAt: null, updatedAt: new Date() }).where(eq(staffs.id, id));
  }
}
