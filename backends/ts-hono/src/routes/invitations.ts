import { Hono } from "hono";
import { current } from "../usecase/invitation/interactor.js";

const app = new Hono();

app.get("/invitation", async (c) => {
  const result = await current();
  return c.json({ found: true, url: result.url, display_url: result.displayUrl, token: result.token });
});

export default app;
