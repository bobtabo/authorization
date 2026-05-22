/**
 * JWT 履歴リポジトリ Drizzle 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { and, eq, isNull, desc } from "drizzle-orm";
import { jwtHistories } from "../model/schema.js";
import type { DB } from "../../db/client.js";

export class DrizzleJwtHistoryRepository {
  constructor(private readonly db: DB) {}

  async findByClientId(clientId: number) {
    return this.db
      .select({
        id: jwtHistories.id,
        memberId: jwtHistories.memberId,
        issueAt: jwtHistories.issueAt,
        jwt: jwtHistories.jwt,
      })
      .from(jwtHistories)
      .where(and(eq(jwtHistories.clientId, clientId), isNull(jwtHistories.deletedAt)))
      .orderBy(desc(jwtHistories.issueAt));
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
