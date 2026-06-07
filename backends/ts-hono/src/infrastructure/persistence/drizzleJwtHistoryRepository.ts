/**
 * JWT 履歴リポジトリ Drizzle 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { and, eq, isNull, desc, asc, sql } from "drizzle-orm";
import { jwtHistories } from "../model/schema.js";
import type { DB } from "../../db/client.js";

const ALLOWED_SORT = new Set(["issue_at", "member_id"]);

export class DrizzleJwtHistoryRepository {
  constructor(private readonly db: DB) {}

  async countByClientId(clientId: number): Promise<number> {
    const result = await this.db
      .select({ count: sql<number>`COUNT(*)` })
      .from(jwtHistories)
      .where(and(eq(jwtHistories.clientId, clientId), isNull(jwtHistories.deletedAt)));
    return Number(result[0]?.count ?? 0);
  }

  async findByCondition(clientId: number, offset: number, limit: number, sort: string, sortType: string) {
    const sortCol = ALLOWED_SORT.has(sort) ? sort : "issue_at";
    const col = sortCol === "member_id" ? jwtHistories.memberId : jwtHistories.issueAt;
    const orderExpr = sortType?.toLowerCase() === "asc" ? asc(col) : desc(col);

    return this.db
      .select({
        id: jwtHistories.id,
        memberId: jwtHistories.memberId,
        issueAt: jwtHistories.issueAt,
        jwt: jwtHistories.jwt,
      })
      .from(jwtHistories)
      .where(and(eq(jwtHistories.clientId, clientId), isNull(jwtHistories.deletedAt)))
      .orderBy(orderExpr)
      .limit(limit > 0 ? limit : 20)
      .offset(offset);
  }

  async save(clientId: number, memberId: string, issueAt: Date, jwt: string): Promise<void> {
    const now = new Date();
    await this.db.insert(jwtHistories).values({
      clientId,
      memberId,
      issueAt,
      jwt,
      createdAt: now,
      createdBy: 0,
      updatedAt: now,
      updatedBy: 0,
      version: 1,
    });
  }
}
