/**
 * クライアントリポジトリ Drizzle 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { and, eq, isNull, like, or, asc, desc, sql, count as drizzleCount } from "drizzle-orm";
import { clients } from "../model/schema.js";
import type { ClientRepository, FindAllOptions } from "../../domain/client/repository.js";
import type { Client } from "../../domain/client/entity.js";
import type { DB } from "../../db/client.js";
import { conflict } from "../../lib/errors.js";

export class DrizzleClientRepository implements ClientRepository {
  constructor(private readonly db: DB) {}

  private buildWhere(keyword?: string, status?: number) {
    const conds = [];
    if (keyword) conds.push(or(like(clients.name, `%${keyword}%`), like(clients.identifier, `%${keyword}%`))!);
    if (status !== undefined) conds.push(eq(clients.status, status));
    return conds.length ? and(...conds) : undefined;
  }

  async findAll(keyword?: string, status?: number, options?: FindAllOptions): Promise<Client[]> {
    const where = this.buildWhere(keyword, status);
    let q = this.db.select().from(clients).where(where).$dynamic();

    if (options?.sort) {
      const col = (clients as Record<string, any>)[options.sort];
      if (col) {
        q = q.orderBy(options.sortType === "desc" ? desc(col) : asc(col));
      } else {
        q = q.orderBy(asc(clients.id));
      }
    } else {
      q = q.orderBy(asc(clients.id));
    }

    if (options?.limit && options.limit > 0) {
      q = q.limit(options.limit).offset(options.offset ?? 0);
    }

    return q;
  }

  async countAll(keyword?: string, status?: number): Promise<number> {
    const where = this.buildWhere(keyword, status);
    const rows = await this.db.select({ value: drizzleCount(clients.id) }).from(clients).where(where);
    return rows[0]?.value ?? 0;
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
