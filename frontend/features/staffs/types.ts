export type StaffActive = "有効" | "無効";
export type StaffRole = "管理者" | "メンバー";

export interface StaffRow {
  id: number;
  name: string;
  email: string;
  active: StaffActive;
  role: StaffRole;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export type SortKey = "name" | "role" | "status" | "created_at";
export type SortOrder = "asc" | "desc";

export const ROLE_MAP: Record<number, StaffRole> = { 1: "管理者", 2: "メンバー" };
export const STATUS_MAP: Record<number, StaffActive> = { 1: "有効", 0: "無効" };
export const ROLE_VALUE: Record<StaffRole, number> = { 管理者: 1, メンバー: 2 };
export const ACTIVE_VALUE: Record<StaffActive, number> = { 有効: 1, 無効: 0 };

export function getRoleBadgeClass(role: StaffRole): string {
  switch (role) {
    case "管理者": return "bg-violet-100 text-violet-800 border border-violet-200";
    case "メンバー": return "bg-sky-100 text-sky-800 border border-sky-200";
  }
}

export function getActiveBadgeClass(active: StaffActive): string {
  return active === "有効"
    ? "bg-emerald-50 text-emerald-800 border border-emerald-200"
    : "bg-slate-300 text-slate-900 border border-slate-500";
}
