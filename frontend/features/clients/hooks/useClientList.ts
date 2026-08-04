import { useEffect, useState } from "react";

import type { Pager as PagerData } from "@/shared/api/types";

import { getClients } from "../api";
import {
  STATUS_MAP,
  STATUS_VALUE,
  type ClientRow,
  type ClientStatus,
  type SortKey,
  type SortOrder,
} from "../types";

export function useClientList() {
  const [rows, setRows] = useState<ClientRow[]>([]);
  const [pager, setPager] = useState<PagerData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [flashMessage, setFlashMessage] = useState<string | null>(null);
  const [flashVisible, setFlashVisible] = useState<boolean>(false);

  const [queryInput, setQueryInput] = useState<string>("");
  const [query, setQuery] = useState<string>("");
  const [sortKey, setSortKey] = useState<SortKey>("created_at");
  const [sortOrder, setSortOrder] = useState<SortOrder>("desc");
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(10);
  const [selectedStatuses, setSelectedStatuses] = useState<ClientStatus[]>([]);
  const [statusFilterOpen, setStatusFilterOpen] = useState<boolean>(false);
  const [startedFromDate, setStartedFromDate] = useState<string>("");
  const [startedToDate, setStartedToDate] = useState<string>("");

  useEffect(() => {
    const msg = sessionStorage.getItem("flashMessage");
    if (msg) {
      sessionStorage.removeItem("flashMessage");
      setFlashMessage(msg);
      setFlashVisible(true);
    }
  }, []);

  useEffect(() => {
    if (!flashMessage || !flashVisible) return;
    const timer = setTimeout(() => setFlashVisible(false), 2000);
    return () => clearTimeout(timer);
  }, [flashMessage, flashVisible]);

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
    let ignore = false;
    setLoading(true);
    setError(null);
    getClients({
      keyword: query || undefined,
      start_from: startedFromDate || undefined,
      start_to: startedToDate || undefined,
      statuses: selectedStatuses.length > 0 ? selectedStatuses.map((s) => STATUS_VALUE[s]) : undefined,
      page: currentPage,
      limit: pageSize,
      sort: sortKey,
      sort_type: sortOrder,
    }).then((res) => {
      if (ignore) return;
      setRows(res.data.map((r) => ({
        id: r.id,
        companyName: r.name,
        status: STATUS_MAP[r.status] ?? "準備中",
        startedAt: r.start_at,
        stoppedAt: r.stop_at,
        createdAt: r.created_at,
        updatedAt: r.updated_at,
      })));
      setPager(res.pager);
    }).catch(() => {
      if (ignore) return;
      setRows([]);
      setPager(null);
      setError("クライアント一覧の取得に失敗しました。");
    }).finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, [query, sortKey, sortOrder, currentPage, pageSize, selectedStatuses, startedFromDate, startedToDate]);

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortKey(key);
      setSortOrder("asc");
    }
    setCurrentPage(1);
  };

  const toggleStatus = (status: ClientStatus) => {
    setSelectedStatuses((prev) =>
      prev.includes(status) ? prev.filter((s) => s !== status) : [...prev, status]
    );
    setCurrentPage(1);
  };

  const allStatuses: ClientStatus[] = ["準備中", "利用中", "停止中", "アーカイブ"];

  const handleClearFilters = () => {
    setQueryInput("");
    setQuery("");
    setSelectedStatuses([]);
    setStartedFromDate("");
    setStartedToDate("");
    setCurrentPage(1);
  };

  const getStatusStyle = (status: ClientStatus) => {
    switch (status) {
      case "利用中":   return "bg-emerald-100 text-emerald-800 border border-emerald-200";
      case "アーカイブ": return "bg-slate-100 text-slate-600 border border-slate-200";
      case "停止中":   return "bg-rose-100 text-rose-700 border border-rose-200";
      case "準備中":   return "bg-amber-100 text-amber-700 border border-amber-200";
    }
  };

  return {
    rows,
    pager,
    loading,
    error,
    flashMessage,
    setFlashMessage,
    flashVisible,
    setFlashVisible,
    queryInput,
    setQueryInput,
    sortKey,
    sortOrder,
    currentPage,
    setCurrentPage,
    pageSize,
    setPageSize,
    selectedStatuses,
    setSelectedStatuses,
    statusFilterOpen,
    setStatusFilterOpen,
    startedFromDate,
    setStartedFromDate,
    startedToDate,
    setStartedToDate,
    handleSort,
    toggleStatus,
    allStatuses,
    handleClearFilters,
    getStatusStyle,
  };
}
