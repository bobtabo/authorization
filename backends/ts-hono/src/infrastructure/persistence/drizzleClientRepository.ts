/**
 * クライアントリポジトリ Drizzle 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { and, eq, isNull, like, or } from "drizzle-orm";
import { clients } from "../model/schema.js";
import type { ClientRepository } from "../../domain/client/repository.js";
import type { Client } from "../../domain/client/entity.js";
import type { DB } from "../../db/client.js";
import { conflict } from "../../lib/errors.js";

export class DrizzleClientRepository implements ClientRepository {
  constructor(private readonly db: DB) {}

  async findAll(keyword?: string, status?: number): Promise<Client[]> {
    const conds = [];
    if (keyword) conds.push(or(like(clients.name, `%${keyword}%`), like(clients.identifier, `%${keyword}%`))!);
    if (status !== undefined) conds.push(eq(clients.status, status));
    return this.db.select().from(clients).where(conds.length ? and(...conds) : undefined).orderBy(clients.id);
  }

  async findById(id: number): Promise<Client | undefined> {
    const rows = await this.db.select().from(clients).where(eq(clients.id, id)).limit(1);
    return rows[0];
  }

  async findByToken(token: string): Promise<Client | undefined> {
    const rows = await this.db.select().from(clients).where(
      and(eq(clients.token, token), eq(clients.status, 2), isNull(clients.deletedAt))
    ).limit(1);
    return rows[0];
  }

  async findByIdentifier(identifier: string): Promise<Client | undefined> {
    const rows = await this.db.select().from(clients).where(and(eq(clients.identifier, identifier), isNull(clients.deletedAt))).limit(1);
    return rows[0];
  }

  async insert(data: typeof clients.$inferInsert): Promise<Client> {
    await this.db.insert(clients).values(data);
    const rows = await this.db.select().from(clients).where(eq(clients.identifier, data.identifier!)).limit(1);
    return rows[0]!;
  }

  async update(id: number, data: Partial<typeof clients.$inferInsert>, version: number): Promise<void> {
    const [result] = await this.db.update(clients)
      .set({ ...data, version: version + 1, updatedAt: new Date() })
      .where(and(eq(clients.id, id), eq(clients.version, version)));
    if (result.affectedRows === 0) throw conflict("optimistic_lock_conflict");
  }

  async softDelete(id: number, version: number): Promise<void> {
    const [result] = await this.db.update(clients)
      .set({ deletedAt: new Date(), version: version + 1, updatedAt: new Date() })
      .where(and(eq(clients.id, id), eq(clients.version, version)));
    if (result.affectedRows === 0) throw conflict("optimistic_lock_conflict");
  }
}
