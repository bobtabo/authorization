/**
 * クライアントルーターモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { Hono } from "hono";
import { formatTime, getStaffIdFromCookie } from "../lib/cookie.js";
import { badRequest } from "../lib/errors.js";
import { db, asTx } from "../db/client.js";
import { DrizzleClientRepository } from "../infrastructure/persistence/drizzleClientRepository.js";
import { DrizzleNotificationRepository } from "../infrastructure/persistence/drizzleNotificationRepository.js";
import { DrizzleStaffRepository } from "../infrastructure/persistence/drizzleStaffRepository.js";
import { ClientInteractor } from "../usecase/client/interactor.js";
import { NotificationInteractor } from "../usecase/notification/interactor.js";
import { sendAccessToken } from "../infrastructure/mail/mailer.js";
import type { ClientListItem, ClientDetailVo } from "../domain/client/valueObjects.js";

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

app.post("/clients/store", async (c) => {
  const body = await c.req.json<Record<string, string>>();
  if (!body.name) throw badRequest("name_required");
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
  sendAccessToken(result.email, result.name, result.token).catch(err => console.error("[sendAccessToken]", err));

  return c.json({ id: result.id, name: result.name, identifier: result.identifier, email: result.email, token: result.token }, 201);
});

app.put("/clients/:id/update", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  const body = await c.req.json<Record<string, unknown>>();

  let result!: ClientDetailVo;
  await db.transaction(async (tx) => {
    const uc = new ClientInteractor(new DrizzleClientRepository(asTx(tx)));
    result = await uc.updateClientData(id, {
      name: body.name as string | undefined,
      postCode: body.post_code as string | undefined,
      pref: body.pref as string | undefined,
      city: body.city as string | undefined,
      address: body.address as string | undefined,
      building: body.building as string | undefined,
      tel: body.tel as string | undefined,
      email: body.email as string | undefined,
      status: body.status as number | undefined,
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

export default app;
