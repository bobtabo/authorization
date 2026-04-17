import { Hono } from "hono";
import { badRequest, unauthorized } from "../lib/errors.js";
import { issueToken, verify } from "../usecase/gate/interactor.js";
import { authenticateByToken } from "../usecase/client/interactor.js";

const app = new Hono();

app.get("/gate/issue", async (c) => {
  const auth = c.req.header("Authorization") ?? "";
  const token = auth.startsWith("Bearer ") ? auth.slice(7) : "";
  if (!token) throw unauthorized("token_required");

  const client = await authenticateByToken(token);
  if (!client) throw unauthorized("invalid_token");

  const member = c.req.query("member");
  if (!member) throw badRequest("member_required");

  const jwt = await issueToken(token, member);
  return c.json({ token: jwt });
});

app.get("/gate/client/:identifier/verify", async (c) => {
  const identifier = c.req.param("identifier");
  const token = c.req.query("token");
  if (!token) throw badRequest("token_required");
  return c.json(await verify(identifier, token));
});

export default app;
