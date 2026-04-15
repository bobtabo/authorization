import { and, eq, isNull, like, or } from "drizzle-orm";
import { db } from "../db/client.js";
import { clients, type Client } from "../db/schema.js";

export async function findAllClients(keyword?: string, status?: number): Promise<Client[]> {
  const conds = [];
  if (keyword) conds.push(or(like(clients.name, `%${keyword}%`), like(clients.identifier, `%${keyword}%`))!);
  if (status !== undefined) conds.push(eq(clients.status, status));
  return db.select().from(clients).where(conds.length ? and(...conds) : undefined).orderBy(clients.id);
}

export async function findClientById(id: number): Promise<Client | undefined> {
  const rows = await db.select().from(clients).where(eq(clients.id, id)).limit(1);
  return rows[0];
}

export async function findClientByToken(token: string): Promise<Client | undefined> {
  const rows = await db.select().from(clients).where(
    and(eq(clients.token, token), eq(clients.status, 2), isNull(clients.deletedAt))
  ).limit(1);
  return rows[0];
}

export async function findClientByIdentifier(identifier: string): Promise<Client | undefined> {
  const rows = await db.select().from(clients).where(and(eq(clients.identifier, identifier), isNull(clients.deletedAt))).limit(1);
  return rows[0];
}

export async function insertClient(data: typeof clients.$inferInsert): Promise<Client> {
  await db.insert(clients).values(data);
  const rows = await db.select().from(clients).where(eq(clients.identifier, data.identifier!)).limit(1);
  return rows[0]!;
}

export async function updateClient(id: number, data: Partial<typeof clients.$inferInsert>): Promise<void> {
  await db.update(clients).set({ ...data, updatedAt: new Date() }).where(eq(clients.id, id));
}

export async function softDeleteClient(id: number): Promise<void> {
  await db.update(clients).set({ deletedAt: new Date(), updatedAt: new Date() }).where(eq(clients.id, id));
}
