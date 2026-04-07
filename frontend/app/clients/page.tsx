"use client";

import React, { useState, useEffect, useMemo } from "react";
import { motion } from "framer-motion";
import {
  Plus,
  Pencil,
  FileSearch,
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  ChevronsLeft,
  ChevronsRight,
  Search,
  ArrowUpDown,
  ArrowUp,
  ArrowDown,
  Building2,
  Eraser,
  Filter,
  CheckCircle2,
  X,
} from "lucide-react";
import { ConsoleHeader } from "@/components/console-header";
import { ConsoleFooter } from "@/components/console-footer";
import { getClients } from "@/src/api/clients";
import { formatTimestamp } from "@/lib/format-datetime";

// =========================
// Types
// =========================

type Status = "準備中" | "利用中" | "停止中" | "アーカイブ";

const STATUS_MAP: Record<number, Status> = {
  1: "準備中",
  2: "利用中",
  3: "停止中",
  4: "アーカイブ",
};

interface ClientType {
  id: number;
  companyName: string;
  status: Status;
  startedAt: string | null;
  stoppedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

type SortKey = keyof Pick<ClientType, "companyName" | "status" | "startedAt" | "stoppedAt" | "createdAt" | "updatedAt">;
type SortOrder = "asc" | "desc";

// =========================
// Component
// =========================

export default function ClientsPage(): React.JSX.Element {
  const [clients, setClients] = useState<ClientType[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [flashMessage, setFlashMessage] = useState<string | null>(null);
  const [flashVisible, setFlashVisible] = useState<boolean>(false);
  const [query, setQuery] = useState<string>("");
  const [sortKey, setSortKey] = useState<SortKey>("createdAt");
  const [sortOrder, setSortOrder] = useState<SortOrder>("desc");

  const [currentPage, setCurrentPage] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(5);
  const [selectedStatuses, setSelectedStatuses] = useState<Status[]>([]);
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

  useEffect(() => {
    getClients().then((res) => {
      const rows = res as Array<Record<string, unknown>>;
      setClients(rows.map((row) => ({
        id: row.id as number,
        companyName: row.name as string,
        status: STATUS_MAP[row.status as number] ?? "準備中",
        startedAt: row.start_at as string | null,
        stoppedAt: row.stop_at as string | null,
        createdAt: row.created_at as string,
        updatedAt: row.updated_at as string,
      })));
    }).finally(() => setLoading(false));
  }, []);

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortKey(key);
      setSortOrder("asc");
    }
  };

  const toggleStatus = (status: Status) => {
    setSelectedStatuses((prev) =>
      prev.includes(status)
        ? prev.filter((s) => s !== status)
        : [...prev, status]
    );
    setCurrentPage(1);
  };

  const allStatuses: Status[] = ["準備中", "利用中", "停止中", "アーカイブ"];

  const parseDateTime = (value: string | null): Date | null => {
    if (!value) return null;
    const [datePart, timePart = "00:00"] = value.split(" ");
    const normalized = `${datePart}T${timePart}`;
    const parsed = new Date(normalized);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  };

  const sortedClients = useMemo<ClientType[]>(() => {
    const fromBoundary = startedFromDate
      ? new Date(`${startedFromDate}T00:00:00`)
      : null;
    const toBoundary = startedToDate
      ? new Date(`${startedToDate}T23:59:59.999`)
      : null;

    const filtered = clients.filter(
      (c) => {
        const matchedName = c.companyName.toLowerCase().includes(query.toLowerCase());
        const matchedStatus =
          selectedStatuses.length === 0 || selectedStatuses.includes(c.status);
        const startedAt = parseDateTime(c.startedAt);
        const matchedStartedFrom = !fromBoundary || (startedAt !== null && startedAt >= fromBoundary);
        const matchedStartedTo = !toBoundary || (startedAt !== null && startedAt <= toBoundary);

        return matchedName && matchedStatus && matchedStartedFrom && matchedStartedTo;
      }
    );

    return [...filtered].sort((a, b) => {
      const aValue = a[sortKey] ?? "";
      const bValue = b[sortKey] ?? "";

      if (aValue < bValue) return sortOrder === "asc" ? -1 : 1;
      if (aValue > bValue) return sortOrder === "asc" ? 1 : -1;
      return 0;
    });
  }, [
    clients,
    query,
    selectedStatuses,
    startedFromDate,
    startedToDate,
    sortKey,
    sortOrder,
  ]);

  const totalPages = Math.max(1, Math.ceil(sortedClients.length / pageSize));
  const safePage = Math.min(currentPage, totalPages);
  const startIndex = (safePage - 1) * pageSize;
  const paginatedClients = sortedClients.slice(startIndex, startIndex + pageSize);

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  const handleClearFilters = () => {
    setQuery("");
    setSelectedStatuses([]);
    setStartedFromDate("");
    setStartedToDate("");
    setCurrentPage(1);
  };

  const getSortIcon = (key: SortKey) => {
    if (sortKey !== key) return <ArrowUpDown size={14} className="opacity-40" />;
    return sortOrder === "asc" ? <ArrowUp size={14} /> : <ArrowDown size={14} />;
  };

  const getStatusStyle = (status: Status) => {
    switch (status) {
      case "利用中":
        return "bg-emerald-100 text-emerald-800 border border-emerald-200";
      case "アーカイブ":
        return "bg-slate-100 text-slate-600 border border-slate-200";
      case "停止中":
        return "bg-rose-100 text-rose-700 border border-rose-200";
      case "準備中":
        return "bg-amber-100 text-amber-700 border border-amber-200";
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <ConsoleHeader />

      <main className="flex-1">
        <div className="max-w-7xl mx-auto px-6 py-10">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-2xl font-semibold text-gray-900 flex items-center gap-2">
              <Building2 size={24} />
              クライアント一覧
            </h1>

            <a
              href="/clients/create"
              className="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
            >
              <Plus size={16} />
              新規登録
            </a>
          </div>

          {/* Search */}
          <div className="mb-4 space-y-3">
            <div className="bg-white border border-gray-200 rounded-lg px-4 py-3">
              <div className="flex flex-wrap items-center gap-3">
                <div className="relative w-full max-w-64">
                  <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    placeholder="会社名で検索..."
                    value={query}
                    onChange={(e) => {
                      setQuery(e.target.value);
                      setCurrentPage(1);
                    }}
                    className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-900 placeholder:text-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  />
                </div>
                <span className="text-sm font-medium text-gray-700">利用開始日</span>
                <input
                  type="date"
                  value={startedFromDate}
                  onChange={(e) => {
                    setStartedFromDate(e.target.value);
                    setCurrentPage(1);
                  }}
                  className="border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-700"
                />
                <span className="text-sm text-gray-500">〜</span>
                <input
                  type="date"
                  value={startedToDate}
                  onChange={(e) => {
                    setStartedToDate(e.target.value);
                    setCurrentPage(1);
                  }}
                  className="border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-700"
                />
                <button
                  type="button"
                  onClick={() => setStatusFilterOpen((prev) => !prev)}
                  className="inline-flex items-center gap-2 border border-indigo-200 bg-indigo-50/70 px-3 py-2 rounded-lg text-sm font-medium text-indigo-700 hover:bg-indigo-100 transition-colors"
                >
                  <Filter size={16} className="shrink-0 opacity-90" aria-hidden />
                  <span>状態</span>
                  <ChevronDown
                    size={16}
                    className={`transition-transform duration-200 ${statusFilterOpen ? "rotate-180" : ""}`}
                  />
                </button>
                <button
                  type="button"
                  onClick={handleClearFilters}
                  className="ml-auto inline-flex items-center gap-1.5 px-3 py-2 text-sm font-medium text-gray-600 border border-gray-300 rounded-md bg-white hover:bg-gray-50"
                >
                  <Eraser size={16} className="text-gray-500 shrink-0" aria-hidden />
                  条件クリア
                </button>
              </div>
            </div>
            {statusFilterOpen && (
              <div className="bg-white border border-indigo-100 rounded-lg px-4 py-3 shadow-sm">
                <div className="flex flex-wrap gap-4">
                  <label className="cursor-pointer">
                    <input
                      type="checkbox"
                      checked={selectedStatuses.length === 0}
                      onChange={() => setSelectedStatuses([])}
                      className="sr-only"
                    />
                    <span
                      className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border transition-all ${
                        selectedStatuses.length === 0
                          ? "bg-indigo-50 text-indigo-700 border-indigo-200 ring-2 ring-offset-1 ring-indigo-100"
                          : "bg-gray-50 text-gray-500 border-gray-200 hover:bg-gray-100"
                      }`}
                    >
                      すべて
                    </span>
                  </label>
                  {allStatuses.map((status) => {
                    const selected = selectedStatuses.includes(status);
                    return (
                      <label key={status} className="cursor-pointer">
                        <input
                          type="checkbox"
                          checked={selected}
                          onChange={() => toggleStatus(status)}
                          className="sr-only"
                        />
                        <span
                          className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border transition-all ${
                            selected
                              ? `${getStatusStyle(status)} ring-2 ring-offset-1 ring-indigo-200`
                              : "bg-gray-50 text-gray-500 border-gray-200 hover:bg-gray-100"
                          }`}
                        >
                          {status}
                        </span>
                      </label>
                    );
                  })}
                </div>
              </div>
            )}
          </div>

          <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
            {loading ? (
              <div className="flex items-center justify-center py-20">
                <div className="w-8 h-8 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin" />
              </div>
            ) : (
              <>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm text-left">
                    <thead className="bg-indigo-50 text-indigo-700 uppercase tracking-wide text-xs border-b border-indigo-100">
                      <tr>
                        <th
                          onClick={() => handleSort("companyName")}
                          className="px-6 py-3 font-medium cursor-pointer select-none"
                        >
                          <div className="flex items-center gap-1">
                            会社名
                            {getSortIcon("companyName")}
                          </div>
                        </th>
                        <th
                          onClick={() => handleSort("status")}
                          className="px-6 py-3 font-medium cursor-pointer select-none"
                        >
                          <div className="flex items-center gap-1">
                            状態
                            {getSortIcon("status")}
                          </div>
                        </th>
                        <th
                          onClick={() => handleSort("startedAt")}
                          className="px-6 py-3 font-medium cursor-pointer select-none"
                        >
                          <div className="flex items-center gap-1">
                            利用開始日時
                            {getSortIcon("startedAt")}
                          </div>
                        </th>
                        <th
                          onClick={() => handleSort("stoppedAt")}
                          className="px-6 py-3 font-medium cursor-pointer select-none"
                        >
                          <div className="flex items-center gap-1">
                            利用停止日時
                            {getSortIcon("stoppedAt")}
                          </div>
                        </th>
                        <th
                          onClick={() => handleSort("createdAt")}
                          className="px-6 py-3 font-medium cursor-pointer select-none"
                        >
                          <div className="flex items-center gap-1">
                            登録日時
                            {getSortIcon("createdAt")}
                          </div>
                        </th>
                        <th
                          onClick={() => handleSort("updatedAt")}
                          className="px-6 py-3 font-medium cursor-pointer select-none"
                        >
                          <div className="flex items-center gap-1">
                            更新日時
                            {getSortIcon("updatedAt")}
                          </div>
                        </th>
                        <th className="px-6 py-3 text-right font-medium">操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      {paginatedClients.length === 0 ? (
                        <tr>
                          <td colSpan={7} className="px-6 py-12 text-center text-gray-500">
                            クライアントが見つかりません
                          </td>
                        </tr>
                      ) : (
                        paginatedClients.map((client) => (
                          <motion.tr
                            key={client.id}
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            className="border-t border-gray-200 hover:bg-gray-50"
                          >
                            <td className="px-6 py-4 font-medium text-gray-900">
                              <a
                                href={`/clients/show?id=${client.id}`}
                                className="group relative inline-flex max-w-full min-w-0 items-center gap-2"
                                aria-label={`${client.companyName}の詳細を表示`}
                              >
                                <span
                                  className="inline-flex shrink-0 rounded-md border border-indigo-100 bg-indigo-50 p-1.5 text-indigo-600 group-hover:border-indigo-200 group-hover:bg-indigo-100"
                                  aria-hidden
                                >
                                  <FileSearch size={16} strokeWidth={2} />
                                </span>
                                <span className="truncate text-indigo-600 group-hover:text-indigo-700 group-hover:underline">
                                  {client.companyName}
                                </span>
                                <span
                                  className="pointer-events-none absolute left-1/2 top-full z-30 mt-1.5 -translate-x-1/2 whitespace-nowrap rounded-md border border-gray-200 bg-white px-2 py-1 text-xs font-medium text-gray-700 opacity-0 shadow-md ring-1 ring-black/5 transition-opacity duration-100 group-hover:opacity-100"
                                  role="tooltip"
                                >
                                  詳細表示します
                                </span>
                              </a>
                            </td>
                            <td className="px-6 py-4">
                              <span
                                className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${getStatusStyle(
                                  client.status
                                )}`}
                              >
                                {client.status}
                              </span>
                            </td>
                            <td className="px-6 py-4 text-gray-600">
                              {formatTimestamp(client.startedAt)}
                            </td>
                            <td className="px-6 py-4 text-gray-600">
                              {formatTimestamp(client.stoppedAt)}
                            </td>
                            <td className="px-6 py-4 text-gray-600">
                              {formatTimestamp(client.createdAt)}
                            </td>
                            <td className="px-6 py-4 text-gray-600">
                              {formatTimestamp(client.updatedAt)}
                            </td>
                            <td className="px-6 py-4 text-right">
                              <a
                                href={`/clients/edit?id=${client.id}`}
                                className="group relative inline-flex p-1 text-gray-500 transition-colors hover:text-indigo-600"
                                aria-label="編集します"
                              >
                                <Pencil size={16} />
                                <span
                                  className="pointer-events-none absolute left-1/2 top-full z-30 mt-1.5 -translate-x-1/2 whitespace-nowrap rounded-md border border-gray-200 bg-white px-2 py-1 text-xs font-medium text-gray-700 opacity-0 shadow-md ring-1 ring-black/5 transition-opacity duration-100 group-hover:opacity-100"
                                  role="tooltip"
                                >
                                  編集します
                                </span>
                              </a>
                            </td>
                          </motion.tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>

                {/* Table Footer */}
                <div className="border-t border-gray-200 px-6 py-4 flex items-center justify-between bg-gray-50">
                  <div className="text-sm text-gray-600">
                    全 {sortedClients.length} 件中 {startIndex + 1} -{" "}
                    {Math.min(startIndex + pageSize, sortedClients.length)} 件表示
                  </div>

                  <div className="flex items-center gap-2 text-gray-700">
                    <select
                      value={pageSize}
                      onChange={(e) => {
                        setPageSize(Number(e.target.value));
                        setCurrentPage(1);
                      }}
                      className="border border-gray-300 bg-white rounded-md px-2 py-1 text-sm text-gray-700"
                    >
                      <option value={5}>5件</option>
                      <option value={10}>10件</option>
                      <option value={20}>20件</option>
                    </select>

                    <button
                      disabled={safePage === 1}
                      onClick={() => setCurrentPage(1)}
                      className="p-1.5 rounded-md border border-gray-300 bg-white text-gray-700 hover:border-indigo-300 hover:text-indigo-700 hover:bg-indigo-50 transition-colors disabled:opacity-35 disabled:hover:border-gray-300 disabled:hover:text-gray-700 disabled:hover:bg-white"
                    >
                      <ChevronsLeft size={16} />
                    </button>
                    <button
                      disabled={safePage === 1}
                      onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                      className="p-1.5 rounded-md border border-gray-300 bg-white text-gray-700 hover:border-indigo-300 hover:text-indigo-700 hover:bg-indigo-50 transition-colors disabled:opacity-35 disabled:hover:border-gray-300 disabled:hover:text-gray-700 disabled:hover:bg-white"
                    >
                      <ChevronLeft size={16} />
                    </button>

                    <span className="text-sm px-2 font-medium text-gray-700">
                      {safePage} / {totalPages}
                    </span>

                    <button
                      disabled={safePage === totalPages}
                      onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                      className="p-1.5 rounded-md border border-gray-300 bg-white text-gray-700 hover:border-indigo-300 hover:text-indigo-700 hover:bg-indigo-50 transition-colors disabled:opacity-35 disabled:hover:border-gray-300 disabled:hover:text-gray-700 disabled:hover:bg-white"
                    >
                      <ChevronRight size={16} />
                    </button>
                    <button
                      disabled={safePage === totalPages}
                      onClick={() => setCurrentPage(totalPages)}
                      className="p-1.5 rounded-md border border-gray-300 bg-white text-gray-700 hover:border-indigo-300 hover:text-indigo-700 hover:bg-indigo-50 transition-colors disabled:opacity-35 disabled:hover:border-gray-300 disabled:hover:text-gray-700 disabled:hover:bg-white"
                    >
                      <ChevronsRight size={16} />
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </main>

      <ConsoleFooter />

      {flashMessage && (
        <div
          style={{
            position: "fixed",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            opacity: flashVisible ? 1 : 0,
            transition: "opacity 0.4s ease-in-out",
            zIndex: 50,
          }}
          onTransitionEnd={(e) => {
            if (e.propertyName === "opacity" && !flashVisible) {
              setFlashMessage(null);
            }
          }}
          className="flex items-center gap-3 bg-white border border-emerald-200 text-emerald-800 text-sm font-medium px-4 py-3 rounded-xl shadow-lg"
        >
          <CheckCircle2 size={18} className="shrink-0 text-emerald-500" />
          {flashMessage}
          <button
            type="button"
            onClick={() => setFlashVisible(false)}
            className="ml-1 text-emerald-400 hover:text-emerald-600"
          >
            <X size={16} />
          </button>
        </div>
      )}
    </div>
  );
}
