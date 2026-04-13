import { Hono } from "hono";
import { current, issue } from "../services/invitationService.js";

const app = new Hono();

app.get("/invitation", async (c) => {
  const result = await current();
  return c.json({ found: true, url: result.url, display_url: result.displayUrl, token: result.token });
});

app.get("/invitation/issue", async (c) => {
  const result = await issue();
  return c.json({ found: true, url: result.url, display_url: result.displayUrl, token: result.token });
});

export default app;
