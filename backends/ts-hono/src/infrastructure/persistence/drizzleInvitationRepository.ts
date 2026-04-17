import { eq } from "drizzle-orm";
import { randomBytes } from "crypto";
import { db } from "../../db/client.js";
import { invitations } from "../model/schema.js";
import type { InvitationRepository } from "../../domain/invitation/repository.js";
import type { Invitation } from "../../domain/invitation/entity.js";

export class DrizzleInvitationRepository implements InvitationRepository {
  async getCurrent(): Promise<Invitation | undefined> {
    const all = await db.select().from(invitations).orderBy(invitations.id);
    return all[all.length - 1];
  }

  async issue(token: string): Promise<Invitation> {
    await db.insert(invitations).values({ token });
    const rows = await db.select().from(invitations).where(eq(invitations.token, token)).limit(1);
    return rows[0]!;
  }

  async findByToken(token: string): Promise<Invitation | undefined> {
    const rows = await db.select().from(invitations).where(eq(invitations.token, token)).limit(1);
    return rows[0];
  }
}
