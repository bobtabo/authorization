import type { Invitation } from "./entity.js";

export interface InvitationRepository {
  getCurrent(): Promise<Invitation | undefined>;
  issue(token: string): Promise<Invitation>;
  findByToken(token: string): Promise<Invitation | undefined>;
}
