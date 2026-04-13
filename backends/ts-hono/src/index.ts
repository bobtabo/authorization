import { serve } from "@hono/node-server";
import { createApp } from "./app.js";
import { config } from "./config.js";

const app = createApp();

const port = config.app.port;
console.log(`starting hono server on :${port}`);
serve({ fetch: app.fetch, port });
