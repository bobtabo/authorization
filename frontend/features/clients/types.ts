export type ClientStatus = "準備中" | "利用中" | "停止中" | "アーカイブ";

export const STATUS_MAP: Record<number, ClientStatus> = {
  1: "準備中",
  2: "利用中",
  3: "停止中",
  4: "アーカイブ",
};

export const STATUS_VALUE: Record<ClientStatus, number> = {
  準備中: 1,
  利用中: 2,
  停止中: 3,
  アーカイブ: 4,
};

export interface ClientRow {
  id: number;
  companyName: string;
  status: ClientStatus;
  startedAt: string | null;
  stoppedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export type SortKey = "name" | "status" | "created_at";
export type SortOrder = "asc" | "desc";

export function getStatusStyle(status: ClientStatus): string {
  switch (status) {
    case "利用中":   return "bg-emerald-100 text-emerald-800 border border-emerald-200";
    case "アーカイブ": return "bg-slate-100 text-slate-600 border border-slate-200";
    case "停止中":   return "bg-rose-100 text-rose-700 border border-rose-200";
    case "準備中":   return "bg-amber-100 text-amber-700 border border-amber-200";
  }
}

export type ClientDetail = {
  clientName: string;
  identifier: string;
  postalCode: string;
  prefecture: string;
  city: string;
  street: string;
  building: string;
  tel: string;
  email: string;
  status: ClientStatus;
  startedAt: string;
  stoppedAt: string;
  createdAt: string;
  updatedAt: string;
};
