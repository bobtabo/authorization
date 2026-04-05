import { apiGet } from "./http";

/** GET /auth/me */
export async function getAuthMe(): Promise<unknown> {
  return apiGet("/auth/me");
}

/** GET /auth/login */
export async function getAuthLogin(): Promise<unknown> {
  return apiGet("/auth/login");
}

/** GET /auth/invitation/{token} */
export async function getAuthInvitation(token: string): Promise<unknown> {
  return apiGet(`/auth/invitation/${encodeURIComponent(token)}`);
}

/** GET /auth/google/redirect */
export async function getAuthGoogleRedirect(): Promise<unknown> {
  return apiGet("/auth/google/redirect");
}

/** GET /auth/google/callback */
export async function getAuthGoogleCallback(
  config?: { params?: Record<string, string | undefined> },
): Promise<unknown> {
  return apiGet("/auth/google/callback", { params: config?.params });
}

/** GET /auth/logout */
export async function getAuthLogout(): Promise<unknown> {
  return apiGet("/auth/logout");
}
