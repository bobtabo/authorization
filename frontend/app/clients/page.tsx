"use client";

import React, { useState, useEffect, useMemo, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  Plus,
  Pencil,
  Trash2,
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  ChevronsLeft,
  ChevronsRight,
  Bell,
  User,
  LogOut,
  Search,
  ArrowUpDown,
  ArrowUp,
  ArrowDown,
  Building2,
  ShieldCheck,
  X,
} from "lucide-react";

// =========================
// Types
// =========================

type Status = "有効" | "無効" | "停止" | "準備中";

interface ClientType {
  id: number;
  companyName: string;
  status: Status;
  startedAt: string | null;
  stoppedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

interface NotificationItem {
  id: number;
  title: string;
  detail: string;
  tone: "info" | "warn" | "ok";
  unread: boolean;
}

type SortKey = keyof Pick<ClientType, "companyName" | "status" | "startedAt" | "stoppedAt" | "createdAt" | "updatedAt">;
type SortOrder = "asc" | "desc";

// =========================
// Component
// =========================

export default function ClientsPage(): React.JSX.Element {
  const [clients, setClients] = useState<ClientType[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [toast, setToast] = useState<string | null>(null);
  const [query, setQuery] = useState<string>("");
  const [sortKey, setSortKey] = useState<SortKey>("createdAt");
  const [sortOrder, setSortOrder] = useState<SortOrder>("desc");
  const [deleteTarget, setDeleteTarget] = useState<ClientType | null>(null);
  const [deleting, setDeleting] = useState<boolean>(false);

  const [currentPage, setCurrentPage] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(5);
  const [selectedStatuses, setSelectedStatuses] = useState<Status[]>([]);
  const [statusFilterOpen, setStatusFilterOpen] = useState<boolean>(false);
  const [startedFromDate, setStartedFromDate] = useState<string>("");
  const [startedToDate, setStartedToDate] = useState<string>("");
  const [useStartedTime, setUseStartedTime] = useState<boolean>(false);
  const [startedFromTime, setStartedFromTime] = useState<string>("00:00");
  const [startedToTime, setStartedToTime] = useState<string>("23:59");
  const [accountMenuOpen, setAccountMenuOpen] = useState<boolean>(false);
  const [notificationOpen, setNotificationOpen] = useState<boolean>(false);
  const [showUnreadOnly, setShowUnreadOnly] = useState<boolean>(false);
  const [notifications, setNotifications] = useState<NotificationItem[]>([
    {
      id: 1,
      title: "新しいクライアントが登録されました",
      detail: "株式会社ABC / 2分前",
      tone: "info",
      unread: true,
    },
    {
      id: 2,
      title: "停止中クライアントが1件あります",
      detail: "JKLホールディングス / 15分前",
      tone: "warn",
      unread: true,
    },
    {
      id: 3,
      title: "定期バックアップが完了しました",
      detail: "本日 09:30",
      tone: "ok",
      unread: false,
    },
  ]);
  const accountMenuRef = useRef<HTMLDivElement | null>(null);
  const notificationRef = useRef<HTMLDivElement | null>(null);
  const unreadCount = notifications.filter((n) => n.unread).length;
  const visibleNotifications = showUnreadOnly
    ? notifications.filter((n) => n.unread)
    : notifications;

  useEffect(() => {
    setTimeout(() => {
      setClients([
        { id: 1, companyName: "株式会社ABC", status: "有効", startedAt: "2026-01-20 10:00", stoppedAt: null, createdAt: "2026-01-15 09:30", updatedAt: "2026-01-20 10:00" },
        { id: 2, companyName: "DEF株式会社", status: "準備中", startedAt: null, stoppedAt: null, createdAt: "2026-01-20 14:00", updatedAt: "2026-01-20 14:00" },
        { id: 3, companyName: "GHI産業", status: "有効", startedAt: "2026-02-05 09:00", stoppedAt: null, createdAt: "2026-02-01 11:00", updatedAt: "2026-02-05 09:00" },
        { id: 4, companyName: "JKLホールディングス", status: "停止", startedAt: "2026-02-10 08:00", stoppedAt: "2026-03-01 18:00", createdAt: "2026-02-05 15:30", updatedAt: "2026-03-01 18:00" },
        { id: 5, companyName: "MNO商事", status: "有効", startedAt: "2026-02-15 10:00", stoppedAt: null, createdAt: "2026-02-10 09:00", updatedAt: "2026-02-15 10:00" },
        { id: 6, companyName: "PQRシステム", status: "無効", startedAt: null, stoppedAt: null, createdAt: "2026-02-15 13:00", updatedAt: "2026-02-20 16:00" },
        { id: 7, companyName: "STUテクノロジー", status: "有効", startedAt: "2026-02-25 09:00", stoppedAt: null, createdAt: "2026-02-20 10:30", updatedAt: "2026-02-25 09:00" },
        { id: 8, companyName: "VWXコンサルティング", status: "準備中", startedAt: null, stoppedAt: null, createdAt: "2026-03-01 08:00", updatedAt: "2026-03-01 08:00" },
      ]);
      setLoading(false);
    }, 800);
  }, []);

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  useEffect(() => {
    if (!accountMenuOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (
        accountMenuRef.current &&
        !accountMenuRef.current.contains(event.target as Node)
      ) {
        setAccountMenuOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [accountMenuOpen]);

  useEffect(() => {
    if (!notificationOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (
        notificationRef.current &&
        !notificationRef.current.contains(event.target as Node)
      ) {
        setNotificationOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [notificationOpen]);

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

  const allStatuses: Status[] = ["有効", "無効", "停止", "準備中"];

  const parseDateTime = (value: string | null): Date | null => {
    if (!value) return null;
    const [datePart, timePart = "00:00"] = value.split(" ");
    const normalized = `${datePart}T${timePart}`;
    const parsed = new Date(normalized);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  };

  const sortedClients = useMemo<ClientType[]>(() => {
    const fromBoundary = startedFromDate
      ? new Date(`${startedFromDate}T${useStartedTime ? startedFromTime : "00:00"}`)
      : null;
    const toBoundary = startedToDate
      ? new Date(`${startedToDate}T${useStartedTime ? startedToTime : "23:59"}`)
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
    useStartedTime,
    startedFromTime,
    startedToTime,
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

  const handleDelete = (id: number) => {
    setDeleting(true);
    setTimeout(() => {
      setClients((prev) => prev.filter((c) => c.id !== id));
      setToast("クライアントを削除しました");
      setDeleteTarget(null);
      setDeleting(false);
    }, 900);
  };

  const handleClearFilters = () => {
    setQuery("");
    setSelectedStatuses([]);
    setStartedFromDate("");
    setStartedToDate("");
    setUseStartedTime(false);
    setStartedFromTime("00:00");
    setStartedToTime("23:59");
    setCurrentPage(1);
  };

  const getSortIcon = (key: SortKey) => {
    if (sortKey !== key) return <ArrowUpDown size={14} className="opacity-40" />;
    return sortOrder === "asc" ? <ArrowUp size={14} /> : <ArrowDown size={14} />;
  };

  const getStatusStyle = (status: Status) => {
    switch (status) {
      case "有効":
        return "bg-emerald-100 text-emerald-800 border border-emerald-200";
      case "無効":
        return "bg-slate-100 text-slate-600 border border-slate-200";
      case "停止":
        return "bg-rose-100 text-rose-700 border border-rose-200";
      case "準備中":
        return "bg-amber-100 text-amber-700 border border-amber-200";
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-6 h-14 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-8 rounded-lg bg-indigo-600 text-white flex items-center justify-center">
              <ShieldCheck size={16} />
            </div>
            <span className="text-sm font-semibold text-gray-800">
              Authorization Console
            </span>
          </div>

          {/* Right Area */}
          <div className="flex items-center gap-6">
            <div ref={notificationRef} className="relative">
              <button
                type="button"
                onClick={() => setNotificationOpen((prev) => !prev)}
                disabled={unreadCount === 0}
                aria-disabled={unreadCount === 0}
                className="relative rounded-lg p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Bell size={18} />
                {unreadCount > 0 && (
                  <span className="absolute -top-0.5 -right-0.5 min-w-4 h-4 px-1 bg-red-500 text-white text-[10px] leading-4 text-center rounded-full font-semibold">
                    {unreadCount}
                  </span>
                )}
              </button>

              <AnimatePresence>
                {notificationOpen && (
                  <motion.div
                    initial={{ opacity: 0, y: -6 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -6 }}
                    className="absolute right-0 top-full mt-2 w-80 rounded-xl border border-gray-200 bg-white shadow-lg overflow-hidden z-20"
                  >
                    <div className="px-4 py-3 border-b border-gray-100 flex items-center justify-between">
                      <p className="text-sm font-semibold text-gray-800">通知</p>
                      <div className="flex items-center gap-3">
                        <button
                          type="button"
                          onClick={() => setShowUnreadOnly((prev) => !prev)}
                          className={`text-xs font-medium ${
                            showUnreadOnly
                              ? "text-indigo-700"
                              : "text-gray-500 hover:text-gray-700"
                          }`}
                        >
                          未読
                        </button>
                        <button
                          type="button"
                          onClick={() =>
                            setNotifications((prev) =>
                              prev.map((item) => ({ ...item, unread: false }))
                            )
                          }
                          className="text-xs text-indigo-600 hover:text-indigo-700 font-medium disabled:text-gray-400"
                          disabled={unreadCount === 0}
                        >
                          全既読
                        </button>
                        <span className="text-xs text-gray-500">未読 {unreadCount}件</span>
                      </div>
                    </div>
                    <div className="max-h-80 overflow-y-auto">
                      {visibleNotifications.length === 0 ? (
                        <div className="px-4 py-8 text-center text-sm text-gray-500">
                          {showUnreadOnly
                            ? "未読なし"
                            : "通知なし"}
                        </div>
                      ) : (
                        visibleNotifications.map((item) => (
                          <div
                            key={item.id}
                            className={`px-4 py-3 border-b border-gray-100 last:border-b-0 hover:bg-gray-50 ${
                              item.unread ? "" : "opacity-60"
                            }`}
                          >
                            <div className="flex items-start gap-2">
                              <span
                                className={`mt-1 h-2 w-2 rounded-full ${
                                  item.tone === "warn"
                                    ? "bg-amber-500"
                                    : item.tone === "ok"
                                      ? "bg-emerald-500"
                                      : "bg-indigo-500"
                                }`}
                              />
                              <div className="min-w-0">
                                <p className="text-sm text-gray-800">{item.title}</p>
                                <p className="text-xs text-gray-500 mt-1">{item.detail}</p>
                              </div>
                              {item.unread && (
                                <span className="ml-auto inline-block px-1.5 py-0.5 text-[10px] rounded bg-indigo-100 text-indigo-700">
                                  NEW
                                </span>
                              )}
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                    <div className="px-4 py-2 bg-gray-50 border-t border-gray-100">
                      <a
                        href="#"
                        className="text-xs text-indigo-600 hover:text-indigo-700 font-medium"
                      >
                        全て表示
                      </a>
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            <div ref={accountMenuRef} className="relative">
              <button
                type="button"
                onClick={() => setAccountMenuOpen((prev) => !prev)}
                className="flex items-center gap-2 rounded-lg px-2 py-1.5 hover:bg-gray-100 transition-colors"
              >
                <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center">
                  <User size={16} />
                </div>
                <span className="text-sm text-gray-700">Platform Admin</span>
                <ChevronDown
                  size={16}
                  className={`text-gray-500 transition-transform duration-200 ${accountMenuOpen ? "rotate-180" : ""}`}
                />
              </button>

              <AnimatePresence>
                {accountMenuOpen && (
                  <motion.div
                    initial={{ opacity: 0, y: -6 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -6 }}
                    className="absolute right-0 top-full mt-2 w-48 rounded-xl border border-gray-200 bg-white shadow-lg overflow-hidden z-20"
                  >
                    <a
                      href="/accounts"
                      onClick={() => setAccountMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50"
                    >
                      <User size={15} />
                      <span>アカウント一覧</span>
                    </a>
                    <a
                      href="/login"
                      onClick={() => setAccountMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 border-t border-gray-100"
                    >
                      <LogOut size={15} />
                      <span>ログアウト</span>
                    </a>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          </div>
        </div>
      </header>

      <main className="flex-1">
        <div className="max-w-7xl mx-auto px-6 py-10">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-2xl font-semibold text-gray-900 flex items-center gap-2">
              <Building2 size={24} />
              クライアント一覧
            </h1>

            <button className="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors">
              <Plus size={16} />
              新規登録
            </button>
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
                    className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  />
                </div>
                <span className="text-sm font-medium text-gray-700">利用開始日時</span>
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
                <label className="inline-flex items-center gap-2 ml-1 text-sm text-gray-600 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={useStartedTime}
                    onChange={(e) => {
                      setUseStartedTime(e.target.checked);
                      setCurrentPage(1);
                    }}
                    className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  時間指定
                </label>
                {useStartedTime && (
                  <>
                    <input
                      type="time"
                      value={startedFromTime}
                      onChange={(e) => {
                        setStartedFromTime(e.target.value);
                        setCurrentPage(1);
                      }}
                      className="border border-gray-300 rounded-md px-2 py-2 text-sm text-gray-700"
                    />
                    <span className="text-sm text-gray-500">〜</span>
                    <input
                      type="time"
                      value={startedToTime}
                      onChange={(e) => {
                        setStartedToTime(e.target.value);
                        setCurrentPage(1);
                      }}
                      className="border border-gray-300 rounded-md px-2 py-2 text-sm text-gray-700"
                    />
                  </>
                )}
                <button
                  type="button"
                  onClick={() => setStatusFilterOpen((prev) => !prev)}
                  className="inline-flex items-center gap-2 border border-indigo-200 bg-indigo-50/70 px-3 py-2 rounded-lg text-sm font-medium text-indigo-700 hover:bg-indigo-100 transition-colors"
                >
                  <span>状態</span>
                  <ChevronDown
                    size={16}
                    className={`transition-transform duration-200 ${statusFilterOpen ? "rotate-180" : ""}`}
                  />
                </button>
                <button
                  type="button"
                  onClick={handleClearFilters}
                  className="ml-auto inline-flex items-center px-3 py-2 text-sm font-medium text-gray-600 border border-gray-300 rounded-md bg-white hover:bg-gray-50"
                >
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
                              {client.companyName}
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
                              {client.startedAt || "-"}
                            </td>
                            <td className="px-6 py-4 text-gray-600">
                              {client.stoppedAt || "-"}
                            </td>
                            <td className="px-6 py-4 text-gray-600">
                              {client.createdAt}
                            </td>
                            <td className="px-6 py-4 text-gray-600">
                              {client.updatedAt}
                            </td>
                            <td className="px-6 py-4 text-right">
                              <div className="flex items-center justify-end gap-2">
                                <button className="p-1 text-gray-500 hover:text-indigo-600 transition-colors">
                                  <Pencil size={16} />
                                </button>
                                <button
                                  onClick={() => setDeleteTarget(client)}
                                  className="p-1 text-gray-500 hover:text-red-600 transition-colors"
                                >
                                  <Trash2 size={16} />
                                </button>
                              </div>
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

      {/* Footer */}
      <footer className="border-t border-gray-200 bg-white py-6 text-center text-xs text-gray-400">
        © 2026 Authorization Console. All rights reserved.
      </footer>

      {/* Delete Modal */}
      <AnimatePresence>
        {deleteTarget && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
            onClick={() => !deleting && setDeleteTarget(null)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
              className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4"
            >
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-gray-900">
                  削除の確認
                </h2>
                <button
                  onClick={() => !deleting && setDeleteTarget(null)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <X size={20} />
                </button>
              </div>

              <p className="text-gray-600 mb-6">
                「{deleteTarget.companyName}」を削除してもよろしいですか？この操作は取り消せません。
              </p>

              <div className="flex gap-3 justify-end">
                <button
                  onClick={() => setDeleteTarget(null)}
                  disabled={deleting}
                  className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 disabled:opacity-50"
                >
                  キャンセル
                </button>
                <button
                  onClick={() => handleDelete(deleteTarget.id)}
                  disabled={deleting}
                  className="px-4 py-2 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 disabled:opacity-50 flex items-center gap-2"
                >
                  {deleting && (
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  )}
                  削除する
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Toast */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
            className="fixed bottom-6 right-6 bg-gray-900 text-white px-4 py-3 rounded-lg shadow-lg text-sm"
          >
            {toast}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
