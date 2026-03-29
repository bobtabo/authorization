"use client";

import React, { useState, useEffect, useMemo } from "react";
import { motion } from "framer-motion";
import {
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  Search,
  ArrowUpDown,
  ArrowUp,
  ArrowDown,
  Users,
  Eraser,
} from "lucide-react";
import { ConsoleHeader } from "@/components/console-header";
import { UserAvatar } from "@/components/user-avatar";

type StaffActive = "有効" | "無効";
type StaffRole = "管理者" | "メンバー";

interface StaffRow {
  id: number;
  name: string;
  email: string;
  active: StaffActive;
  role: StaffRole;
  createdAt: string;
  updatedAt: string;
}

type SortKey = keyof Pick<
  StaffRow,
  "name" | "email" | "role" | "active" | "createdAt" | "updatedAt"
>;
type SortOrder = "asc" | "desc";

function getRoleBadgeClass(role: StaffRole): string {
  switch (role) {
    case "管理者":
      return "bg-violet-100 text-violet-800 border border-violet-200";
    case "メンバー":
      return "bg-sky-100 text-sky-800 border border-sky-200";
  }
}

function getActiveBadgeClass(active: StaffActive): string {
  return active === "有効"
    ? "bg-emerald-50 text-emerald-800 border border-emerald-200"
    : // 無効ON: 未選択のグレーと差がつくよう少し濃く
      "bg-slate-300 text-slate-900 border border-slate-500";
}

const segmentInactive = "text-gray-500 hover:text-gray-700 hover:bg-gray-100/80";

function RoleSegmentSwitch({
  role,
  onChange,
  ariaLabel,
}: {
  role: StaffRole;
  onChange: (r: StaffRole) => void;
  ariaLabel: string;
}): React.JSX.Element {
  return (
    <div
      className="inline-flex shrink-0 rounded-full border border-gray-200 bg-gray-50/90 p-0.5 shadow-sm"
      role="group"
      aria-label={ariaLabel}
    >
      <button
        type="button"
        onClick={() => onChange("メンバー")}
        className={`rounded-full px-2 py-0.5 text-[11px] font-semibold leading-tight transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 focus-visible:ring-offset-1 ${
          role === "メンバー"
            ? `${getRoleBadgeClass("メンバー")} shadow-sm`
            : segmentInactive
        }`}
      >
        メンバー
      </button>
      <button
        type="button"
        onClick={() => onChange("管理者")}
        className={`rounded-full px-2 py-0.5 text-[11px] font-semibold leading-tight transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 focus-visible:ring-offset-1 ${
          role === "管理者"
            ? `${getRoleBadgeClass("管理者")} shadow-sm`
            : segmentInactive
        }`}
      >
        管理者
      </button>
    </div>
  );
}

function ActiveSegmentSwitch({
  active,
  onChange,
  ariaLabel,
}: {
  active: StaffActive;
  onChange: (a: StaffActive) => void;
  ariaLabel: string;
}): React.JSX.Element {
  return (
    <div
      className="inline-flex shrink-0 rounded-full border border-gray-200 bg-gray-50/90 p-0.5 shadow-sm"
      role="group"
      aria-label={ariaLabel}
    >
      <button
        type="button"
        onClick={() => onChange("無効")}
        className={`rounded-full px-2 py-0.5 text-[11px] font-semibold leading-tight transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 focus-visible:ring-offset-1 ${
          active === "無効"
            ? `${getActiveBadgeClass("無効")} shadow-sm`
            : segmentInactive
        }`}
      >
        無効
      </button>
      <button
        type="button"
        onClick={() => onChange("有効")}
        className={`rounded-full px-2 py-0.5 text-[11px] font-semibold leading-tight transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 focus-visible:ring-offset-1 ${
          active === "有効"
            ? `${getActiveBadgeClass("有効")} shadow-sm`
            : segmentInactive
        }`}
      >
        有効
      </button>
    </div>
  );
}

export default function StaffPage(): React.JSX.Element {
  const [staff, setStaff] = useState<StaffRow[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [query, setQuery] = useState<string>("");
  const [sortKey, setSortKey] = useState<SortKey>("createdAt");
  const [sortOrder, setSortOrder] = useState<SortOrder>("desc");

  const [currentPage, setCurrentPage] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(5);
  const [selectedActiveFilters, setSelectedActiveFilters] = useState<StaffActive[]>([]);
  const [selectedRoleFilters, setSelectedRoleFilters] = useState<StaffRole[]>([]);

  useEffect(() => {
    setTimeout(() => {
      setStaff([
        {
          id: 1,
          name: "山田 太郎",
          email: "yamada@example.com",
          active: "有効",
          role: "管理者",
          createdAt: "2026-01-15 09:30",
          updatedAt: "2026-01-20 10:00",
        },
        {
          id: 2,
          name: "佐藤 花子",
          email: "sato@example.com",
          active: "有効",
          role: "メンバー",
          createdAt: "2026-01-20 14:00",
          updatedAt: "2026-01-20 14:00",
        },
        {
          id: 3,
          name: "鈴木 一郎",
          email: "suzuki@example.com",
          active: "無効",
          role: "メンバー",
          createdAt: "2026-02-01 11:00",
          updatedAt: "2026-02-05 09:00",
        },
        {
          id: 4,
          name: "田中 美咲",
          email: "tanaka@example.com",
          active: "有効",
          role: "管理者",
          createdAt: "2026-02-05 15:30",
          updatedAt: "2026-03-01 18:00",
        },
        {
          id: 5,
          name: "伊藤 健",
          email: "ito@example.com",
          active: "有効",
          role: "メンバー",
          createdAt: "2026-02-10 09:00",
          updatedAt: "2026-02-15 10:00",
        },
        {
          id: 6,
          name: "渡辺 直子",
          email: "watanabe@example.com",
          active: "無効",
          role: "管理者",
          createdAt: "2026-02-15 13:00",
          updatedAt: "2026-02-20 16:00",
        },
        {
          id: 7,
          name: "中村 翔",
          email: "nakamura@example.com",
          active: "有効",
          role: "メンバー",
          createdAt: "2026-02-20 10:30",
          updatedAt: "2026-02-25 09:00",
        },
        {
          id: 8,
          name: "小林 誠",
          email: "kobayashi@example.com",
          active: "有効",
          role: "メンバー",
          createdAt: "2026-03-01 08:00",
          updatedAt: "2026-03-01 08:00",
        },
      ]);
      setLoading(false);
    }, 800);
  }, []);

  const setStaffRowActive = (id: number, active: StaffActive) => {
    setStaff((prev) => prev.map((a) => (a.id === id ? { ...a, active } : a)));
  };

  const setStaffRowRole = (id: number, role: StaffRole) => {
    setStaff((prev) => prev.map((a) => (a.id === id ? { ...a, role } : a)));
  };

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortKey(key);
      setSortOrder("asc");
    }
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

  const sortedStaff = useMemo<StaffRow[]>(() => {
    const q = query.trim().toLowerCase();
    const filtered = staff.filter((a) => {
      const matchedQuery =
        q === "" ||
        a.name.toLowerCase().includes(q) ||
        a.email.toLowerCase().includes(q);
      const matchedActive =
        selectedActiveFilters.length === 0 || selectedActiveFilters.includes(a.active);
      const matchedRole =
        selectedRoleFilters.length === 0 || selectedRoleFilters.includes(a.role);
      return matchedQuery && matchedActive && matchedRole;
    });

    const activeRank = (x: StaffActive): number => (x === "有効" ? 0 : 1);

    return [...filtered].sort((a, b) => {
      if (sortKey === "active") {
        const diff = activeRank(a.active) - activeRank(b.active);
        if (diff !== 0) return sortOrder === "asc" ? diff : -diff;
        return 0;
      }

      const aValue = a[sortKey] ?? "";
      const bValue = b[sortKey] ?? "";

      if (aValue < bValue) return sortOrder === "asc" ? -1 : 1;
      if (aValue > bValue) return sortOrder === "asc" ? 1 : -1;
      return 0;
    });
  }, [staff, query, selectedActiveFilters, selectedRoleFilters, sortKey, sortOrder]);

  const totalPages = Math.max(1, Math.ceil(sortedStaff.length / pageSize));
  const safePage = Math.min(currentPage, totalPages);
  const startIndex = (safePage - 1) * pageSize;
  const paginatedStaff = sortedStaff.slice(startIndex, startIndex + pageSize);

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  const handleClearFilters = () => {
    setQuery("");
    setSelectedActiveFilters([]);
    setSelectedRoleFilters([]);
    setCurrentPage(1);
  };

  const getSortIcon = (key: SortKey) => {
    if (sortKey !== key) return <ArrowUpDown size={14} className="opacity-40" />;
    return sortOrder === "asc" ? <ArrowUp size={14} /> : <ArrowDown size={14} />;
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <ConsoleHeader />

      <main className="flex-1">
        <div className="max-w-7xl mx-auto px-6 py-10">
          <div className="mb-6">
            <h1 className="text-2xl font-semibold text-gray-900 flex items-center gap-2">
              <Users size={24} />
              スタッフ一覧
            </h1>
          </div>

          <div className="mb-4">
            <div className="bg-white border border-gray-200 rounded-lg px-4 py-4">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between sm:gap-x-8">
                <div className="flex min-w-0 flex-wrap items-center gap-3">
                  <div className="relative w-full shrink-0 sm:w-[18rem]">
                    <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="text"
                      placeholder="名前・メールで検索"
                      value={query}
                      onChange={(e) => {
                        setQuery(e.target.value);
                        setCurrentPage(1);
                      }}
                      className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-900 placeholder:text-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    />
                  </div>

                  <fieldset className="w-fit max-w-full shrink-0 rounded-lg border border-gray-200 bg-gray-50/80 px-2.5 py-2">
                    <legend className="px-0.5 text-xs font-semibold text-gray-600">権限</legend>
                    <div className="mt-0.5 flex flex-wrap gap-1.5">
                      <label className="cursor-pointer">
                        <input
                          type="checkbox"
                          checked={selectedRoleFilters.length === 0}
                          onChange={() => setSelectedRoleFilters([])}
                          className="sr-only"
                        />
                        <span
                          className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${
                            selectedRoleFilters.length === 0
                              ? "bg-white text-indigo-700 border-indigo-200 ring-2 ring-indigo-100"
                              : "bg-white/80 text-gray-500 border-gray-200 hover:bg-white"
                          }`}
                        >
                          すべて
                        </span>
                      </label>
                      {allRoleFilters.map((role) => {
                        const selected = selectedRoleFilters.includes(role);
                        return (
                          <label key={role} className="cursor-pointer">
                            <input
                              type="checkbox"
                              checked={selected}
                              onChange={() => toggleRoleFilter(role)}
                              className="sr-only"
                            />
                            <span
                              className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${
                                selected
                                  ? `${getRoleBadgeClass(role)} ring-2 ring-offset-1 ring-indigo-100`
                                  : "bg-white/80 text-gray-500 border-gray-200 hover:bg-white"
                              }`}
                            >
                              {role}
                            </span>
                          </label>
                        );
                      })}
                    </div>
                  </fieldset>

                  <fieldset className="w-fit max-w-full shrink-0 rounded-lg border border-gray-200 bg-gray-50/80 px-2.5 py-2">
                    <legend className="px-0.5 text-xs font-semibold text-gray-600 whitespace-nowrap">
                      状態
                    </legend>
                    <div className="mt-0.5 flex flex-wrap gap-1.5">
                      <label className="cursor-pointer">
                        <input
                          type="checkbox"
                          checked={selectedActiveFilters.length === 0}
                          onChange={() => setSelectedActiveFilters([])}
                          className="sr-only"
                        />
                        <span
                          className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${
                            selectedActiveFilters.length === 0
                              ? "bg-white text-indigo-700 border-indigo-200 ring-2 ring-indigo-100"
                              : "bg-white/80 text-gray-500 border-gray-200 hover:bg-white"
                          }`}
                        >
                          すべて
                        </span>
                      </label>
                      {allActiveFilters.map((act) => {
                        const selected = selectedActiveFilters.includes(act);
                        return (
                          <label key={act} className="cursor-pointer">
                            <input
                              type="checkbox"
                              checked={selected}
                              onChange={() => toggleActiveFilter(act)}
                              className="sr-only"
                            />
                            <span
                              className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${
                                selected
                                  ? `${getActiveBadgeClass(act)} ring-2 ring-offset-1 ring-indigo-100`
                                  : "bg-white/80 text-gray-500 border-gray-200 hover:bg-white"
                              }`}
                            >
                              {act}
                            </span>
                          </label>
                        );
                      })}
                    </div>
                  </fieldset>
                </div>

                <button
                  type="button"
                  onClick={handleClearFilters}
                  className="inline-flex shrink-0 items-center justify-center gap-1.5 self-end px-3 py-2 text-sm font-medium text-gray-600 border border-gray-300 rounded-md bg-white hover:bg-gray-50 sm:self-center"
                >
                  <Eraser size={16} className="text-gray-500 shrink-0" aria-hidden />
                  条件クリア
                </button>
              </div>
            </div>
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
                          onClick={() => handleSort("name")}
                          className="px-6 py-3 font-medium cursor-pointer select-none"
                        >
                          <div className="flex items-center gap-1">
                            名前
                            {getSortIcon("name")}
                          </div>
                        </th>
                        <th
                          onClick={() => handleSort("email")}
                          className="px-6 py-3 font-medium cursor-pointer select-none"
                        >
                          <div className="flex items-center gap-1">
                            メールアドレス
                            {getSortIcon("email")}
                          </div>
                        </th>
                        <th
                          onClick={() => handleSort("role")}
                          className="px-6 py-3 font-medium cursor-pointer select-none"
                        >
                          <div className="flex items-center gap-1">
                            権限
                            {getSortIcon("role")}
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
                        <th
                          onClick={() => handleSort("active")}
                          className="px-6 py-3 text-right font-medium cursor-pointer select-none normal-case"
                        >
                          <div className="flex items-center justify-end gap-1">
                            状態
                            {getSortIcon("active")}
                          </div>
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {paginatedStaff.length === 0 ? (
                        <tr>
                          <td colSpan={6} className="px-6 py-12 text-center text-gray-500">
                            スタッフが見つかりません
                          </td>
                        </tr>
                      ) : (
                        paginatedStaff.map((row) => (
                          <motion.tr
                            key={row.id}
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            className="border-t border-gray-200 hover:bg-gray-50"
                          >
                            <td className="px-6 py-4 font-medium text-gray-900">
                              <div className="flex min-h-[1.75rem] items-center gap-2.5 min-w-0">
                                <UserAvatar name={row.name} />
                                <span className="truncate leading-tight">{row.name}</span>
                              </div>
                            </td>
                            <td className="px-6 py-4 text-gray-600 break-all max-w-[14rem]">
                              {row.email}
                            </td>
                            <td className="px-6 py-4">
                              <div className="flex min-h-[1.75rem] items-center">
                                <RoleSegmentSwitch
                                  role={row.role}
                                  onChange={(r) => setStaffRowRole(row.id, r)}
                                  ariaLabel={`${row.name}の権限`}
                                />
                              </div>
                            </td>
                            <td className="px-6 py-4 text-gray-600 whitespace-nowrap">
                              {row.createdAt}
                            </td>
                            <td className="px-6 py-4 text-gray-600 whitespace-nowrap">
                              {row.updatedAt}
                            </td>
                            <td className="px-6 py-4 text-right">
                              <div className="flex min-h-[1.75rem] items-center justify-end">
                                <ActiveSegmentSwitch
                                  active={row.active}
                                  onChange={(a) => setStaffRowActive(row.id, a)}
                                  ariaLabel={`${row.name}の状態`}
                                />
                              </div>
                            </td>
                          </motion.tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>

                <div className="border-t border-gray-200 px-6 py-4 flex items-center justify-between bg-gray-50">
                  <div className="text-sm text-gray-600">
                    全 {sortedStaff.length} 件中 {startIndex + 1} -{" "}
                    {Math.min(startIndex + pageSize, sortedStaff.length)} 件表示
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

      <footer className="border-t border-gray-200 bg-white py-6 text-center text-xs text-gray-400">
        © 2026 Authorization Console. All rights reserved.
      </footer>
    </div>
  );
}
