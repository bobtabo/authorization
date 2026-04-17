import { randomBytes } from "crypto";
import { notFound } from "../../lib/errors.js";
import { DrizzleInvitationRepository } from "../../infrastructure/persistence/drizzleInvitationRepository.js";
import { config } from "../../config.js";
import type { Invitation } from "../../domain/invitation/entity.js";
import type { InvitationResult } from "../../domain/invitation/valueObjects.js";

const repo = new DrizzleInvitationRepository();

function buildResult(token: string): InvitationResult {
  const url = `${config.app.frontendUrl}/invitation/${token}`;
  return { token, url, displayUrl: url.replace(/^https?:\/\//, "") };
}

export async function current(): Promise<InvitationResult> {
  const inv = await repo.getCurrent();
  if (!inv) throw notFound("invitation_not_found");
  return buildResult(inv.token);
}

export async function issue(): Promise<InvitationResult> {
  const token = randomBytes(16).toString("hex");
  const inv = await repo.issue(token);
  return buildResult(inv.token);
}

export async function findByToken(token: string): Promise<Invitation> {
  const inv = await repo.findByToken(token);
  if (!inv) throw notFound("invitation_not_found");
  return inv;
}
