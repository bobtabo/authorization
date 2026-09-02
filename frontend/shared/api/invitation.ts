import { apiGet } from "./http";

export type InvitationUrlResponse = {
  url: string;
  display_url: string;
  token: string;
};

/** GET /admin/invitation?role={role} */
export async function getInvitation(role: 1 | 2 = 2): Promise<InvitationUrlResponse> {
  return apiGet<InvitationUrlResponse>(`/admin/invitation?role=${role}`);
}

/** GET /admin/invitation/issue?role={role} */
export async function issueInvitation(role: 1 | 2 = 2): Promise<InvitationUrlResponse> {
  return apiGet<InvitationUrlResponse>(`/admin/invitation/issue?role=${role}`);
}
