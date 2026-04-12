import { and, eq, inArray, isNull, like, or } from "drizzle-orm";
import { db } from "../db/client.js";
import { staffs, type Staff } from "../db/schema.js";

export async function findAllStaffs(keyword?: string, roles?: number[]): Promise<Staff[]> {
  const conds = [];
  if (keyword) conds.push(or(like(staffs.name, `%${keyword}%`), like(staffs.email, `%${keyword}%`))!);
  if (roles && roles.length > 0) conds.push(inArray(staffs.role, roles));
  return db.select().from(staffs).where(conds.length ? and(...conds) : undefined).orderBy(staffs.id);
}

export async function findStaffByProvider(provider: number, providerId: string): Promise<Staff | undefined> {
  const rows = await db.select().from(staffs)
    .where(and(eq(staffs.provider, provider), eq(staffs.providerId, providerId), isNull(staffs.deletedAt)))
    .limit(1);
  return rows[0];
}

export async function findStaffById(id: number): Promise<Staff | undefined> {
  const rows = await db.select().from(staffs).where(and(eq(staffs.id, id), isNull(staffs.deletedAt))).limit(1);
  return rows[0];
}

export async function findStaffByIdUnscoped(id: number): Promise<Staff | undefined> {
  const rows = await db.select().from(staffs).where(eq(staffs.id, id)).limit(1);
  return rows[0];
}

export async function findAllActiveStaffs(): Promise<Staff[]> {
  return db.select().from(staffs).where(isNull(staffs.deletedAt));
}

export async function upsertStaff(data: typeof staffs.$inferInsert): Promise<Staff> {
  const existing = await findStaffByProvider(data.provider, data.providerId);
  if (existing) {
    await db.update(staffs).set({ name: data.name, email: data.email, avatar: data.avatar, updatedAt: new Date() }).where(eq(staffs.id, existing.id));
    return (await db.select().from(staffs).where(eq(staffs.id, existing.id)).limit(1))[0]!;
  }
  await db.insert(staffs).values(data);
  return (await db.select().from(staffs).where(and(eq(staffs.provider, data.provider), eq(staffs.providerId, data.providerId))).limit(1))[0]!;
}

export async function updateStaffRole(id: number, role: number): Promise<void> {
  await db.update(staffs).set({ role, updatedAt: new Date() }).where(eq(staffs.id, id));
}

export async function softDeleteStaff(id: number): Promise<void> {
  await db.update(staffs).set({ deletedAt: new Date(), updatedAt: new Date() }).where(eq(staffs.id, id));
}

export async function restoreStaff(id: number): Promise<void> {
  await db.update(staffs).set({ deletedAt: null, updatedAt: new Date() }).where(eq(staffs.id, id));
}
