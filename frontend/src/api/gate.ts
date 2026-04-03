import { apiGet } from "./http";

/** GET /gate/issue?member=…（Sanctum 要） */
export async function getGateIssue(member: string): Promise<unknown> {
  return apiGet("/gate/issue", { params: { member } });
}

/** GET /gate/client/{identifier}/verify?token=… */
export async function getGateVerify(
  identifier: string,
  token: string,
): Promise<unknown> {
  return apiGet(`/gate/client/${encodeURIComponent(identifier)}/verify`, {
    params: { token },
  });
}
