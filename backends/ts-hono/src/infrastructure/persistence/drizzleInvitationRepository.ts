/**
 * 招待リポジトリ Drizzle 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { eq } from "drizzle-orm";
import { invitations } from "../model/schema.js";
import type { InvitationRepository } from "../../domain/invitation/repository.js";
import type { Invitation } from "../../domain/invitation/entity.js";
import type { DB } from "../../db/client.js";

export class DrizzleInvitationRepository implements InvitationRepository {
  constructor(private readonly db: DB) {}

  async getCurrent(): Promise<Invitation | undefined> {
    const all = await this.db.select().from(invitations).orderBy(invitations.id);
    return all[all.length - 1];
  }

  async issue(token: string): Promise<Invitation> {
    await this.db.insert(invitations).values({ token, createdBy: 0, updatedBy: 0 });
    const rows = await this.db.select().from(invitations).where(eq(invitations.token, token)).limit(1);
    return rows[0]!;
  }

  async findByToken(token: string): Promise<Invitation | undefined> {
    const rows = await this.db.select().from(invitations).where(eq(invitations.token, token)).limit(1);
    return rows[0];
  }
}
