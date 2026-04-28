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

const invitationAuthRepo = new RedisInvitationAuthRepository();

const app = new Hono();

app.get("/invitation", async (c) => {
  const uc = new InvitationInteractor(new DrizzleInvitationRepository(db), invitationAuthRepo);
  const result = await uc.current();
  return c.json({ found: true, url: result.url, display_url: result.displayUrl, token: result.token });
});

app.get("/invitation/issue", async (c) => {
  let result!: Awaited<ReturnType<InvitationInteractor["issue"]>>;
  await db.transaction(async (tx) => {
    const uc = new InvitationInteractor(new DrizzleInvitationRepository(asTx(tx)), invitationAuthRepo);
    result = await uc.issue();
  });
  return c.json({ found: true, url: result.url, display_url: result.displayUrl, token: result.token });
});

export default app;
