import { Hono } from "hono";
import { formatTime } from "../lib/cookie.js";
import { badRequest } from "../lib/errors.js";
import {
  getAllClients, getClientById, storeClient, updateClientData, destroyClient,
} from "../usecase/client/interactor.js";
import type { Client } from "../domain/client/entity.js";

const app = new Hono();

function mapClient(c: Client) {
  return {
    id: c.id, name: c.name, identifier: c.identifier,
    post_code: c.postCode, pref: c.pref, city: c.city,
    address: c.address, building: c.building, tel: c.tel, email: c.email,
    status: c.status, token: c.token, fingerprint: c.fingerprint,
    started_at: formatTime(c.startedAt), stopped_at: formatTime(c.stoppedAt),
    created_at: formatTime(c.createdAt), updated_at: formatTime(c.updatedAt),
  };
}

app.get("/clients", async (c) => {
  const keyword = c.req.query("keyword");
  const statusStr = c.req.query("status");
  const status = statusStr !== undefined ? parseInt(statusStr, 10) : undefined;
  const list = await getAllClients(keyword, status);
  return c.json(list.map(mapClient));
});

app.get("/clients/:id", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  return c.json(mapClient(await getClientById(id)));
});

app.post("/clients/store", async (c) => {
  const body = await c.req.json<Record<string, string>>();
  if (!body.name || !body.identifier) throw badRequest("name_and_identifier_required");
  const client = await storeClient({
    name: body.name, identifier: body.identifier,
    postCode: body.post_code, pref: body.pref, city: body.city,
    address: body.address, building: body.building, tel: body.tel, email: body.email,
  });
  return c.json(mapClient(client), 201);
});

app.put("/clients/:id/update", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  const body = await c.req.json<Record<string, unknown>>();
  const client = await updateClientData(id, {
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
  return c.json(mapClient(client));
});

app.delete("/clients/:id/delete", async (c) => {
  const id = parseInt(c.req.param("id"), 10);
  await destroyClient(id);
  return c.json({ id });
});

export default app;
