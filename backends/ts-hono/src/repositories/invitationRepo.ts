import { eq } from "drizzle-orm";
import { db } from "../db/client.js";
import { invitations, type Invitation } from "../db/schema.js";
import { randomBytes } from "crypto";

export interface InvitationResult {
  token: string;
  url: string;
  displayUrl: string;
}

function buildResult(token: string, frontendUrl: string): InvitationResult {
  const url = `${frontendUrl}/invitation/${token}`;
  return { token, url, displayUrl: url.replace(/^https?:\/\//, "") };
}

export async function getCurrent(frontendUrl: string): Promise<InvitationResult | undefined> {
  const rows = await db.select().from(invitations).orderBy(invitations.id).limit(1);
  // 最新を取得
  const all = await db.select().from(invitations).orderBy(invitations.id);
  const last = all[all.length - 1];
  if (!last) return undefined;
  return buildResult(last.token, frontendUrl);
}

export async function issueInvitation(frontendUrl: string): Promise<InvitationResult> {
  const token = randomBytes(16).toString("hex");
  await db.insert(invitations).values({ token });
  return buildResult(token, frontendUrl);
}

export async function findInvitationByToken(token: string): Promise<Invitation | undefined> {
  const rows = await db.select().from(invitations).where(eq(invitations.token, token)).limit(1);
  return rows[0];
}
