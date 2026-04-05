import { apiGet } from "./http";

export type InvitationUrlResponse = {
  url: string;
};

/** GET /invitation */
export async function getInvitation(): Promise<InvitationUrlResponse> {
  return apiGet<InvitationUrlResponse>("/invitation");
}

/** GET /invitation/issue */
export async function issueInvitation(): Promise<InvitationUrlResponse> {
  return apiGet<InvitationUrlResponse>("/invitation/issue");
}
