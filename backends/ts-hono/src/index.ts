import { serve } from "@hono/node-server";
import { Hono } from "hono";
import { cors } from "hono/cors";
import { AppError } from "./lib/errors.js";
import { config } from "./config.js";
import authRoutes, { oauthApp } from "./routes/auth.js";
import clientRoutes from "./routes/clients.js";
import staffRoutes from "./routes/staffs.js";
import invitationRoutes from "./routes/invitations.js";
import gateRoutes from "./routes/gate.js";
import notificationRoutes from "./routes/notifications.js";

const app = new Hono();

app.use("*", cors({
  origin: config.app.frontendUrl,
  allowMethods: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
  allowHeaders: ["Content-Type", "Authorization"],
  credentials: true,
}));

app.onError((err, c) => {
  if (err instanceof AppError) {
    return c.json({ message: err.message }, err.statusCode as Parameters<typeof c.json>[1]);
  }
  console.error(err);
  return c.json({ message: "internal_server_error" }, 500);
});

// OAuth はブラウザリダイレクトのため /api 外に配置（PHP と同じパス構造）
app.route("/", oauthApp);

const api = app.basePath("/api");
api.route("/", authRoutes);
api.route("/", clientRoutes);
api.route("/", staffRoutes);
api.route("/", invitationRoutes);
api.route("/", gateRoutes);
api.route("/", notificationRoutes);

const port = config.app.port;
console.log(`starting hono server on :${port}`);
serve({ fetch: app.fetch, port });
