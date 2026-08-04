import { useEffect, useState } from "react";

import { getAuthMe } from "@/shared/api/auth";
import type { Pager as PagerData } from "@/shared/api/types";

import { getStaffs, updateStaffRole, deleteStaff, restoreStaff } from "../api";
import {
  ACTIVE_VALUE,
  ROLE_MAP,
  ROLE_VALUE,
  STATUS_MAP,
  type SortKey,
  type SortOrder,
  type StaffActive,
  type StaffRole,
  type StaffRow,
} from "../types";

export function useStaffList() {
  const [rows, setRows] = useState<StaffRow[]>([]);
  const [pager, setPager] = useState<PagerData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [mutationError, setMutationError] = useState<string | null>(null);
  const [myStaffId, setMyStaffId] = useState<number | null>(null);
  const [authLoading, setAuthLoading] = useState<boolean>(true);

  const [queryInput, setQueryInput] = useState<string>("");
  const [query, setQuery] = useState<string>("");
  const [sortKey, setSortKey] = useState<SortKey>("created_at");
  const [sortOrder, setSortOrder] = useState<SortOrder>("desc");
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(10);
  const [selectedActiveFilters, setSelectedActiveFilters] = useState<StaffActive[]>([]);
  const [selectedRoleFilters, setSelectedRoleFilters] = useState<StaffRole[]>([]);

  useEffect(() => {
    getAuthMe()
      .then((res) => {
        const me = res as Record<string, unknown>;
        setMyStaffId(typeof me?.staff_id === "number" ? me.staff_id : null);
      })
      .catch(() => setMyStaffId(null))
      .finally(() => setAuthLoading(false));
  }, []);

  useEffect(() => {
    if (!mutationError) return;
    const t = setTimeout(() => setMutationError(null), 3000);
    return () => clearTimeout(t);
  }, [mutationError]);

  // キーワードをデバウンス
  useEffect(() => {
    const t = setTimeout(() => {
      setQuery(queryInput);
      setCurrentPage(1);
    }, 400);
    return () => clearTimeout(t);
  }, [queryInput]);

  // データ取得
  useEffect(() => {
    setLoading(true);
    setError(null);
    let ignore = false;
    getStaffs({
      keyword: query || undefined,
      roles: selectedRoleFilters.length > 0 ? selectedRoleFilters.map((r) => ROLE_VALUE[r]) : undefined,
      statuses: selectedActiveFilters.length > 0 ? selectedActiveFilters.map((a) => ACTIVE_VALUE[a]) : undefined,
      page: currentPage,
      limit: pageSize,
      sort: sortKey,
      sort_type: sortOrder,
    }).then((res) => {
      if (ignore) return;
      setRows(res.data.map((r) => ({
        id: r.id,
        name: r.name,
        email: r.email,
        role: ROLE_MAP[r.role] ?? "メンバー",
        active: STATUS_MAP[r.status] ?? "有効",
        createdAt: r.created_at,
        updatedAt: r.updated_at,
        version: r.version ?? 1,
      })));
      setPager(res.pager);
    }).catch(() => {
      if (ignore) return;
      setRows([]);
      setPager(null);
      setError("スタッフ一覧の取得に失敗しました。");
    }).finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, [query, sortKey, sortOrder, currentPage, pageSize, selectedActiveFilters, selectedRoleFilters]);

  const setRowActive = (id: number, active: StaffActive) => {
    const found = rows.find((r) => r.id === id);
    const prev = found?.active;
    const version = found?.version ?? 1;
    setRows((s) => s.map((r) => (r.id === id ? { ...r, active } : r)));
    const apiCall = active === "無効" ? deleteStaff(id, { version }, myStaffId) : restoreStaff(id, myStaffId);
    apiCall.then(() => {
      // サーバーはバージョンを1つ進める。次回のインライン編集が古いversionで送られないよう反映しておく
      setRows((s) => s.map((r) => (r.id === id ? { ...r, version: r.version + 1 } : r)));
    }).catch(() => {
      if (prev !== undefined) setRows((s) => s.map((r) => (r.id === id ? { ...r, active: prev } : r)));
      setMutationError("状態の更新に失敗しました。");
    });
  };

  const setRowRole = (id: number, role: StaffRole) => {
    const found = rows.find((r) => r.id === id);
    const prev = found?.role;
    const version = found?.version ?? 1;
    setRows((s) => s.map((r) => (r.id === id ? { ...r, role } : r)));
    updateStaffRole(id, { role: ROLE_VALUE[role], version }, myStaffId).then(() => {
      setRows((s) => s.map((r) => (r.id === id ? { ...r, version: r.version + 1 } : r)));
    }).catch(() => {
      if (prev !== undefined) setRows((s) => s.map((r) => (r.id === id ? { ...r, role: prev } : r)));
      setMutationError("権限の更新に失敗しました。");
    });
  };

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortKey(key);
      setSortOrder("asc");
    }
    setCurrentPage(1);
  };

  const toggleActiveFilter = (value: StaffActive) => {
    setSelectedActiveFilters((prev) =>
      prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value]
    );
    setCurrentPage(1);
  };

  const toggleRoleFilter = (value: StaffRole) => {
    setSelectedRoleFilters((prev) =>
      prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value]
    );
    setCurrentPage(1);
  };

  const allActiveFilters: StaffActive[] = ["有効", "無効"];
  const allRoleFilters: StaffRole[] = ["管理者", "メンバー"];

  const handleClearFilters = () => {
    setQueryInput("");
    setQuery("");
    setSelectedActiveFilters([]);
    setSelectedRoleFilters([]);
    setCurrentPage(1);
  };

  return {
    rows,
    pager,
    loading,
    error,
    mutationError,
    myStaffId,
    authLoading,
    queryInput,
    setQueryInput,
    sortKey,
    sortOrder,
    setCurrentPage,
    setPageSize,
    selectedActiveFilters,
    setSelectedActiveFilters,
    selectedRoleFilters,
    setSelectedRoleFilters,
    setRowActive,
    setRowRole,
    handleSort,
    toggleActiveFilter,
    toggleRoleFilter,
    allActiveFilters,
    allRoleFilters,
    handleClearFilters,
  };
}
