import { notFound } from "../lib/errors.js";
import { getCurrent, issueInvitation, findInvitationByToken } from "../repositories/invitationRepo.js";
import type { InvitationResult } from "../repositories/invitationRepo.js";
import type { Invitation } from "../db/schema.js";
import { config } from "../config.js";

export async function current(): Promise<InvitationResult> {
  const result = await getCurrent(config.app.frontendUrl);
  if (!result) throw notFound("invitation_not_found");
  return result;
}

export async function issue(): Promise<InvitationResult> {
  return issueInvitation(config.app.frontendUrl);
}

export async function findByToken(token: string): Promise<Invitation> {
  const inv = await findInvitationByToken(token);
  if (!inv) throw notFound("invitation_not_found");
  return inv;
}
