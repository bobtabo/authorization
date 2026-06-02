/**
 * クライアントルーターモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { Hono } from "hono";
import { z } from "zod";
import { config } from "../config.js";
import { formatTime, getStaffIdFromCookie } from "../lib/cookie.js";
import { badRequest, AppError } from "../lib/errors.js";
import { db, asTx } from "../db/client.js";
import { DrizzleClientRepository } from "../infrastructure/persistence/drizzleClientRepository.js";
import { DrizzleJwtHistoryRepository } from "../infrastructure/persistence/drizzleJwtHistoryRepository.js";
import { DrizzleNotificationRepository } from "../infrastructure/persistence/drizzleNotificationRepository.js";
import { DrizzleStaffRepository } from "../infrastructure/persistence/drizzleStaffRepository.js";
import { ClientInteractor } from "../usecase/client/interactor.js";
import { NotificationInteractor } from "../usecase/notification/interactor.js";
import { sendActivation } from "../infrastructure/mail/mailer.js";
import type { ClientListItem, ClientDetailVo } from "../domain/client/valueObjects.js";

function formatTimeSec(d: Date | null | undefined): string | null {
  if (!d) return null;
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

const storeClientSchema = z.object({
  name:      z.string().min(1).max(255),
  post_code: z.string().min(1).max(8),
  pref:      z.string().min(1).max(50),
  city:      z.string().min(1).max(100),
  address:   z.string().min(1).max(255),
  building:  z.string().max(255).optional().default(""),
  tel:       z.string().regex(/^\d{10,11}$/),
  email:     z.string().email().max(255),
});

const updateClientSchema = z.object({
  name:      z.string().max(255).optional(),
  post_code: z.string().max(8).optional(),
  pref:      z.string().max(50).optional(),
  city:      z.string().max(100).optional(),
  address:   z.string().max(255).optional(),
  building:  z.string().max(255).optional(),
  tel:       z.string().regex(/^\d{10,11}$/).optional(),
  email:     z.string().email().max(255).optional(),
  status:    z.number().int().optional(),
});

const app = new Hono();

function mapListItem(c: ClientListItem) {
  return {
    id: c.id, name: c.name, identifier: c.identifier, status: c.status,
    start_at: formatTime(c.startedAt), stop_at: formatTime(c.stoppedAt),
    created_at: formatTime(c.createdAt), updated_at: formatTime(c.updatedAt),
  };
}

function mapDetail(c: ClientDetailVo) {
  return {
    id: c.id, name: c.name, identifier: c.identifier,
    post_code: c.postCode, pref: c.pref, city: c.city,
    address: c.address, building: c.building, tel: c.tel, email: c.email,
    status: c.status, fingerprint: c.fingerprint,
    start_at: formatTime(c.startedAt), stop_at: formatTime(c.stoppedAt),
    created_at: formatTime(c.createdAt), updated_at: formatTime(c.updatedAt),
  };
}

app.get("/clients", async (c) => {
  const keyword = c.req.query("keyword");
  const statusStr = c.req.query("status");
  const status = statusStr !== undefined ? parseInt(statusStr, 10) : undefined;
  const uc = new ClientInteractor(new DrizzleClientRepository(db));
  const list = await uc.getAllClients(keyword, status);
  return c.json(list.map(mapListItem));
});

app.get("/clients/:id", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  const uc = new ClientInteractor(new DrizzleClientRepository(db));
  return c.json(mapDetail(await uc.getClientById(id)));
});

app.get("/clients/:id/jwt-histories", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  const repo = new DrizzleJwtHistoryRepository(db);
  const histories = await repo.findByClientId(id);
  return c.json(histories.map((h) => ({
    id: h.id,
    member_id: h.memberId,
    issue_at: formatTimeSec(h.issueAt),
    jwt: h.jwt,
  })));
});

app.post("/clients/store", async (c) => {
  const raw = await c.req.json();
  const parsed = storeClientSchema.safeParse(raw);
  if (!parsed.success) throw new AppError(422, "validation_error");
  const body = parsed.data;
  const executorId = getStaffIdFromCookie(c);

  let result!: Awaited<ReturnType<ClientInteractor["storeClient"]>>;
  await db.transaction(async (tx) => {
    const uc = new ClientInteractor(new DrizzleClientRepository(asTx(tx)));
    result = await uc.storeClient({
      name: body.name,
      postCode: body.post_code, pref: body.pref, city: body.city,
      address: body.address, building: body.building, tel: body.tel, email: body.email,
      executorId,
    });
  });

  const notifUrl = `/clients/show?id=${result.id}`;
  const notifUc = new NotificationInteractor(new DrizzleNotificationRepository(db), new DrizzleStaffRepository(db));
  notifUc.fanOut("新しいクライアントが登録されました", result.name, notifUrl, executorId, 1).catch(err => console.error("[fanOut]", err));
  const activateUrl = `${config.app.frontendUrl}/clients/${result.identifier}/qr`;
  sendActivation(result.email, result.name, activateUrl).catch(err => console.error("[sendActivation]", err));

  return c.json({ id: result.id, name: result.name, identifier: result.identifier, email: result.email, token: result.token }, 201);
});

app.put("/clients/:id/update", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  const raw = await c.req.json();
  const parsed = updateClientSchema.safeParse(raw);
  if (!parsed.success) throw new AppError(422, "validation_error");
  const body = parsed.data;

  let result!: ClientDetailVo;
  await db.transaction(async (tx) => {
    const uc = new ClientInteractor(new DrizzleClientRepository(asTx(tx)));
    result = await uc.updateClientData(id, {
      name: body.name,
      postCode: body.post_code,
      pref: body.pref,
      city: body.city,
      address: body.address,
      building: body.building,
      tel: body.tel,
      email: body.email,
      status: body.status,
    });
  });
  return c.json(mapDetail(result));
});

app.delete("/clients/:id/delete", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  await db.transaction(async (tx) => {
    const uc = new ClientInteractor(new DrizzleClientRepository(asTx(tx)));
    await uc.destroyClient(id);
  });
  return c.json({ id });
});

// --- スマホアプリ連携エンドポイント ---

app.get("/clients/:identifier/qr", async (c) => {
  const identifier = c.req.param("identifier");
  const uc = new ClientInteractor(new DrizzleClientRepository(db));
  const vo = await uc.getQr(identifier);
  return c.json({ identifier: vo.identifier, deeplink_url: vo.deeplinkUrl });
});

app.get("/clients/:identifier/info", async (c) => {
  const identifier = c.req.param("identifier");
  const uc = new ClientInteractor(new DrizzleClientRepository(db));
  const vo = await uc.getInfo(identifier);
  return c.json({ identifier: vo.identifier, name: vo.name, status: vo.status });
});

app.patch("/clients/:identifier/start", async (c) => {
  const identifier = c.req.param("identifier");
  let accessToken!: string;
  await db.transaction(async (tx) => {
    const uc = new ClientInteractor(new DrizzleClientRepository(asTx(tx)));
    const vo = await uc.startClient(identifier);
    accessToken = vo.accessToken;
  });
  return c.json({ access_token: accessToken });
});

app.patch("/clients/:identifier/stop", async (c) => {
  const identifier = c.req.param("identifier");
  await db.transaction(async (tx) => {
    const uc = new ClientInteractor(new DrizzleClientRepository(asTx(tx)));
    await uc.stopClient(identifier);
  });
  return c.json({});
});

export default app;
