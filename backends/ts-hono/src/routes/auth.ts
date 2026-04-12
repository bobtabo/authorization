import { Hono } from "hono";
import { setCookie, deleteCookie } from "hono/cookie";
import { config } from "../config.js";
import { badRequest, unauthorized } from "../lib/errors.js";
import { getStaffIdFromCookie } from "../lib/cookie.js";
import { findUser, login } from "../services/authService.js";
import { findByToken } from "../services/invitationService.js";

const app = new Hono();

// OAuth はブラウザリダイレクトのため /api 外に配置（PHP と同じパス構造）
export const oauthApp = new Hono();

const GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
const GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
const GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

app.get("/auth/me", async (c) => {
  const staffId = getStaffIdFromCookie(c);
  if (!staffId) throw unauthorized("unauthenticated");
  const staff = await findUser(staffId);
  if (!staff) throw unauthorized("unauthenticated");
  return c.json({ staff_id: staff.id, name: staff.name, avatar: staff.avatar, role: staff.role });
});

app.get("/auth/login", (c) => {
  return c.json({ login_url: `${config.app.frontendUrl}/login` });
});

app.get("/auth/logout", (c) => {
  deleteCookie(c, "staff_id", { path: "/" });
  return c.json({ message: "logged_out" });
});

app.get("/auth/invitation/:token", async (c) => {
  const token = c.req.param("token");
  const inv = await findByToken(token);
  return c.json({ token: inv.token });
});

oauthApp.get("/auth/google/redirect", (c) => {
  const params = new URLSearchParams({
    client_id: config.oauth.googleClientId,
    redirect_uri: config.oauth.googleRedirectUrl,
    response_type: "code",
    scope: "openid email profile",
    access_type: "offline",
  });
  return c.redirect(`${GOOGLE_AUTH_URL}?${params.toString()}`, 302);
});

oauthApp.get("/auth/google/callback", async (c) => {
  const code = c.req.query("code");
  if (!code) throw badRequest("code_required");

  const tokenRes = await fetch(GOOGLE_TOKEN_URL, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      code,
      client_id: config.oauth.googleClientId,
      client_secret: config.oauth.googleClientSecret,
      redirect_uri: config.oauth.googleRedirectUrl,
      grant_type: "authorization_code",
    }),
  });
  const tokenData = await tokenRes.json() as { access_token?: string };
  if (!tokenData.access_token) throw unauthorized("token_exchange_failed");

  const userRes = await fetch(GOOGLE_USERINFO_URL, {
    headers: { Authorization: `Bearer ${tokenData.access_token}` },
  });
  const userInfo = await userRes.json() as { id: string; name?: string; email?: string; picture?: string };

  const staff = await login(1, userInfo.id, userInfo.name ?? "", userInfo.email ?? "", userInfo.picture);
  const maxAge = config.app.staffCookieLifetime * 60;
  setCookie(c, "staff_id", String(staff.id), { maxAge, httpOnly: true, sameSite: "Lax", path: "/" });
  return c.redirect(`${config.app.frontendUrl}/clients`, 302);
});

export default app;
