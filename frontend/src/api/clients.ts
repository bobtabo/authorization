import { apiDelete, apiGet, apiPost, apiPut } from "./http";

export type ClientsQuery = {
  keyword?: string;
  start_from?: string;
  start_to?: string;
  statuses?: number[];
  page?: number;
  limit?: number;
  sort?: string;
  sort_type?: string;
};

export type ClientApiRow = {
  id: number;
  name: string;
  status: number;
  start_at: string | null;
  stop_at: string | null;
  created_at: string;
  updated_at: string;
};

/** GET /clients */
export async function getClients(params?: ClientsQuery): Promise<{ data: ClientApiRow[]; pager: Pager }> {
  return apiGet<{ data: ClientApiRow[]; pager: Pager }>("/clients", { params });
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
  return apiGet<ClientQr>(`/clients/${encodeURIComponent(identifier)}/qr`);
}

export type JwtHistory = {
  id: number;
  member_id: string;
  issue_at: string;
  jwt: string;
};

export type Pager = {
  count: number;
  limit: number;
  next: boolean;
  previous: boolean;
  page: number;
  nextPage: number;
  previousPage: number;
  pageCount: number;
  first: boolean;
  last: boolean;
  firstRecordCount: number;
  lastRecordCount: number;
  startPage: number;
  endPage: number;
};

export type JwtHistoriesQuery = {
  page?: number;
  limit?: number;
  sort?: string;
  sort_type?: string;
};

/** GET /clients/{id}/jwt-histories — JWT履歴一覧取得 */
export async function getJwtHistories(
  clientId: number | string,
  params?: JwtHistoriesQuery,
): Promise<{ data: JwtHistory[]; pager: Pager }> {
  return apiGet<{ data: JwtHistory[]; pager: Pager }>(`/clients/${clientId}/jwt-histories`, { params });
}

