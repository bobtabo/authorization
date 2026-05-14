import { apiDelete, apiGet, apiPost, apiPut } from "./http";

export type ClientsQuery = {
  keyword?: string;
  start_from?: string;
  start_to?: string;
  statuses?: number[];
};

/** GET /clients */
export async function getClients(params?: ClientsQuery): Promise<unknown> {
  return apiGet("/clients", { params });
}

/** GET /clients/{id} */
export async function getClient(id: number | string): Promise<unknown> {
  return apiGet(`/clients/${id}`);
}

/** POST /clients/store */
export async function createClient(body: unknown): Promise<unknown> {
  return apiPost("/clients/store", body);
}

/** PUT /clients/{id}/update */
export async function updateClient(id: number | string, body: unknown): Promise<unknown> {
  return apiPut(`/clients/${id}/update`, body);
}

/** DELETE /clients/{id}/delete */
export async function deleteClient(id: number | string, body: { version: number }): Promise<unknown> {
  return apiDelete(`/clients/${id}/delete`, { data: body });
}

export type ClientQr = {
  identifier: string;
  deeplink_url: string;
};

/** GET /clients/{identifier}/qr — スマホアプリ連携用QRコードデータ取得 */
export async function getClientQr(identifier: string): Promise<ClientQr> {
  return apiGet<ClientQr>(`/clients/${identifier}/qr`);
}
