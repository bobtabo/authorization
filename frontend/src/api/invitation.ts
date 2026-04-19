import { apiGet } from "./http";

export type InvitationUrlResponse = {
  url: string;
  display_url: string;
  token: string;
};

/** GET /admin/invitation */
export async function getInvitation(): Promise<InvitationUrlResponse> {
  return apiGet<InvitationUrlResponse>("/admin/invitation");
}

/** GET /admin/invitation/issue */
export async function issueInvitation(): Promise<InvitationUrlResponse> {
  return apiGet<InvitationUrlResponse>("/admin/invitation/issue");
}
