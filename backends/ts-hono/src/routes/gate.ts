/**
 * Gate ルーターモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { Hono } from "hono";
import { badRequest, unauthorized } from "../lib/errors.js";
import { db } from "../db/client.js";
import { DrizzleClientRepository } from "../infrastructure/persistence/drizzleClientRepository.js";
import { DrizzleJwtHistoryRepository } from "../infrastructure/persistence/drizzleJwtHistoryRepository.js";
import { RedisGateRepository } from "../infrastructure/cache/redisGateRepository.js";
import { GateInteractor } from "../usecase/gate/interactor.js";

const app = new Hono();

app.get("/gate/issue", async (c) => {
  const auth = c.req.header("Authorization") ?? "";
  const token = auth.startsWith("Bearer ") ? auth.slice(7) : "";
  if (!token) throw unauthorized("token_required");

  const member = c.req.query("member");
  if (!member) throw badRequest("member_required");

  const uc = new GateInteractor(new DrizzleClientRepository(db), new RedisGateRepository(), new DrizzleJwtHistoryRepository(db));
  const vo = await uc.issueToken(token, member);
  return c.json({ token: vo.token });
});

app.get("/gate/client/:identifier/verify", async (c) => {
  const identifier = c.req.param("identifier");
  const token = c.req.query("token");
  if (!token) throw badRequest("token_required");

  const uc = new GateInteractor(new DrizzleClientRepository(db), new RedisGateRepository());
  const vo = await uc.verify(identifier, token);
  return c.json({ identifier: vo.identifier, member: vo.member, fingerprint: vo.fingerprint, payload: vo.payload });
});

export default app;
