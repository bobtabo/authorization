/**
 * 管理者招待ルーターモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { Hono } from "hono";
import { db, asTx } from "../db/client.js";
import { DrizzleInvitationRepository } from "../infrastructure/persistence/drizzleInvitationRepository.js";
import { RedisInvitationAuthRepository } from "../infrastructure/cache/redisInvitationAuthRepository.js";
import { InvitationInteractor } from "../usecase/invitation/interactor.js";
import { badRequest, unauthorized } from "../lib/errors.js";
import { getStaffIdFromCookie } from "../lib/cookie.js";

const invitationAuthRepo = new RedisInvitationAuthRepository();

const app = new Hono();

function parseRole(raw: string | undefined): number {
  const role = raw !== undefined ? parseInt(raw, 10) : 2;
  if (role !== 1 && role !== 2) throw badRequest("invalid_role");
  return role;
}

app.get("/invitation", async (c) => {
  const role = parseRole(c.req.query("role"));
  const uc = new InvitationInteractor(new DrizzleInvitationRepository(db), invitationAuthRepo);
  const result = await uc.current(role);
  return c.json({ found: true, url: result.url, display_url: result.displayUrl, token: result.token });
});

app.get("/invitation/issue", async (c) => {
  const staffId = getStaffIdFromCookie(c);
  if (!staffId) throw unauthorized("unauthenticated");
  const role = parseRole(c.req.query("role"));
  let result!: Awaited<ReturnType<InvitationInteractor["issue"]>>;
  await db.transaction(async (tx) => {
    const uc = new InvitationInteractor(new DrizzleInvitationRepository(asTx(tx)), invitationAuthRepo);
    result = await uc.issue(role);
  });
  return c.json({ found: true, url: result.url, display_url: result.displayUrl, token: result.token });
});

export default app;
