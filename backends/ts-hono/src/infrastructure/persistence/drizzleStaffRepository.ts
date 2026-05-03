/**
 * スタッフリポジトリ Drizzle 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { and, eq, inArray, isNull, like, or } from "drizzle-orm";
import { staffs } from "../model/schema.js";
import type { StaffRepository } from "../../domain/staff/repository.js";
import type { Staff } from "../../domain/staff/entity.js";
import type { DB } from "../../db/client.js";
import { conflict } from "../../lib/errors.js";

export class DrizzleStaffRepository implements StaffRepository {
  constructor(private readonly db: DB) {}

  async findAll(keyword?: string, roles?: number[]): Promise<Staff[]> {
    const conds = [];
    if (keyword) conds.push(or(like(staffs.name, `%${keyword}%`), like(staffs.email, `%${keyword}%`))!);
    if (roles && roles.length > 0) conds.push(inArray(staffs.role, roles));
    return this.db.select().from(staffs).where(conds.length ? and(...conds) : undefined).orderBy(staffs.id);
  }

  async findById(id: number): Promise<Staff | undefined> {
    const rows = await this.db.select().from(staffs).where(and(eq(staffs.id, id), isNull(staffs.deletedAt))).limit(1);
    return rows[0];
  }

  async findByIdUnscoped(id: number): Promise<Staff | undefined> {
    const rows = await this.db.select().from(staffs).where(eq(staffs.id, id)).limit(1);
    return rows[0];
  }

  async findByProvider(provider: number, providerId: string): Promise<Staff | undefined> {
    const rows = await this.db.select().from(staffs)
      .where(and(eq(staffs.provider, provider), eq(staffs.providerId, providerId)))
      .limit(1);
    return rows[0];
  }

  async findAllActive(): Promise<Staff[]> {
    return this.db.select().from(staffs).where(isNull(staffs.deletedAt));
  }

  async upsert(data: typeof staffs.$inferInsert): Promise<Staff> {
    const existing = await this.findByProvider(data.provider, data.providerId);
    if (existing) {
      await this.db.update(staffs).set({ name: data.name, email: data.email, avatar: data.avatar, lastLoginAt: data.lastLoginAt ?? null, updatedAt: new Date() }).where(eq(staffs.id, existing.id));
      return (await this.db.select().from(staffs).where(eq(staffs.id, existing.id)).limit(1))[0]!;
    }
    await this.db.insert(staffs).values({ ...data, createdBy: 0, updatedBy: 0 });
    return (await this.db.select().from(staffs).where(and(eq(staffs.provider, data.provider), eq(staffs.providerId, data.providerId))).limit(1))[0]!;
  }

  async updateRole(id: number, role: number, version: number): Promise<void> {
    const [result] = await this.db.update(staffs)
      .set({ role, version: version + 1, updatedAt: new Date() })
      .where(and(eq(staffs.id, id), eq(staffs.version, version)));
    if (result.affectedRows === 0) throw conflict("optimistic_lock_conflict");
  }

  async softDelete(id: number, version: number): Promise<void> {
    const [result] = await this.db.update(staffs)
      .set({ deletedAt: new Date(), version: version + 1, updatedAt: new Date() })
      .where(and(eq(staffs.id, id), eq(staffs.version, version)));
    if (result.affectedRows === 0) throw conflict("optimistic_lock_conflict");
  }

  async restore(id: number, version: number): Promise<void> {
    const [result] = await this.db.update(staffs)
      .set({ deletedAt: null, version: version + 1, updatedAt: new Date() })
      .where(and(eq(staffs.id, id), eq(staffs.version, version)));
    if (result.affectedRows === 0) throw conflict("optimistic_lock_conflict");
  }
}
