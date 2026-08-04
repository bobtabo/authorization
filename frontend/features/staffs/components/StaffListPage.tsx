"use client";

import { AnimatePresence, motion } from "framer-motion";
import {
  Search,
  ArrowUpDown,
  ArrowUp,
  ArrowDown,
  Users,
  Eraser,
} from "lucide-react";
import React from "react";

import { ConsoleFooter } from "@/shared/components/console-footer";
import { ConsoleHeader } from "@/shared/components/console-header";
import { Pager } from "@/shared/components/pager";
import { UserAvatar } from "@/shared/components/user-avatar";

import { useStaffList } from "../hooks/useStaffList";
import {
  getActiveBadgeClass,
  getRoleBadgeClass,
  type SortKey,
  type StaffActive,
  type StaffRole,
} from "../types";

const segmentInactive = "text-gray-500 hover:text-gray-700 hover:bg-gray-100/80";

function RoleSegmentSwitch({
  role, onChange, ariaLabel, disabled = false,
}: { role: StaffRole; onChange: (r: StaffRole) => void; ariaLabel: string; disabled?: boolean }): React.JSX.Element {
  return (
    <div className={`inline-flex shrink-0 rounded-full border border-gray-200 bg-gray-50/90 p-0.5 shadow-sm ${disabled ? "opacity-40 cursor-not-allowed" : ""}`} role="group" aria-label={ariaLabel}>
      <button type="button" disabled={disabled} aria-pressed={role === "メンバー"} onClick={() => onChange("メンバー")}
        className={`rounded-full px-2 py-0.5 text-[11px] font-semibold leading-tight transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 focus-visible:ring-offset-1 disabled:pointer-events-none ${role === "メンバー" ? `${getRoleBadgeClass("メンバー")} shadow-sm` : segmentInactive}`}>
        メンバー
      </button>
      <button type="button" disabled={disabled} aria-pressed={role === "管理者"} onClick={() => onChange("管理者")}
        className={`rounded-full px-2 py-0.5 text-[11px] font-semibold leading-tight transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 focus-visible:ring-offset-1 disabled:pointer-events-none ${role === "管理者" ? `${getRoleBadgeClass("管理者")} shadow-sm` : segmentInactive}`}>
        管理者
      </button>
    </div>
  );
}

function ActiveSegmentSwitch({
  active, onChange, ariaLabel, disabled = false,
}: { active: StaffActive; onChange: (a: StaffActive) => void; ariaLabel: string; disabled?: boolean }): React.JSX.Element {
  return (
    <div className={`inline-flex shrink-0 rounded-full border border-gray-200 bg-gray-50/90 p-0.5 shadow-sm ${disabled ? "opacity-40 cursor-not-allowed" : ""}`} role="group" aria-label={ariaLabel}>
      <button type="button" disabled={disabled} aria-pressed={active === "無効"} onClick={() => onChange("無効")}
        className={`rounded-full px-2 py-0.5 text-[11px] font-semibold leading-tight transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 focus-visible:ring-offset-1 disabled:pointer-events-none ${active === "無効" ? `${getActiveBadgeClass("無効")} shadow-sm` : segmentInactive}`}>
        無効
      </button>
      <button type="button" disabled={disabled} aria-pressed={active === "有効"} onClick={() => onChange("有効")}
        className={`rounded-full px-2 py-0.5 text-[11px] font-semibold leading-tight transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 focus-visible:ring-offset-1 disabled:pointer-events-none ${active === "有効" ? `${getActiveBadgeClass("有効")} shadow-sm` : segmentInactive}`}>
        有効
      </button>
    </div>
  );
}

export function StaffListPage(): React.JSX.Element {
  const {
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
  } = useStaffList();

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
                      value={queryInput}
                      onChange={(e) => setQueryInput(e.target.value)}
                      className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-900 placeholder:text-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    />
                  </div>

                  <fieldset className="w-fit max-w-full shrink-0 rounded-lg border border-gray-200 bg-gray-50/80 px-2.5 py-2">
                    <legend className="px-0.5 text-xs font-semibold text-gray-600">権限</legend>
                    <div className="mt-0.5 flex flex-wrap gap-1.5">
                      <label className="cursor-pointer">
                        <input type="checkbox" checked={selectedRoleFilters.length === 0} onChange={() => { setSelectedRoleFilters([]); setCurrentPage(1); }} className="sr-only" />
                        <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${selectedRoleFilters.length === 0 ? "bg-white text-indigo-700 border-indigo-200 ring-2 ring-indigo-100" : "bg-white/80 text-gray-500 border-gray-200 hover:bg-white"}`}>
                          すべて
                        </span>
                      </label>
                      {allRoleFilters.map((role) => {
                        const selected = selectedRoleFilters.includes(role);
                        return (
                          <label key={role} className="cursor-pointer">
                            <input type="checkbox" checked={selected} onChange={() => toggleRoleFilter(role)} className="sr-only" />
                            <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${selected ? `${getRoleBadgeClass(role)} ring-2 ring-offset-1 ring-indigo-100` : "bg-white/80 text-gray-500 border-gray-200 hover:bg-white"}`}>
                              {role}
                            </span>
                          </label>
                        );
                      })}
                    </div>
                  </fieldset>

                  <fieldset className="w-fit max-w-full shrink-0 rounded-lg border border-gray-200 bg-gray-50/80 px-2.5 py-2">
                    <legend className="px-0.5 text-xs font-semibold text-gray-600 whitespace-nowrap">状態</legend>
                    <div className="mt-0.5 flex flex-wrap gap-1.5">
                      <label className="cursor-pointer">
                        <input type="checkbox" checked={selectedActiveFilters.length === 0} onChange={() => { setSelectedActiveFilters([]); setCurrentPage(1); }} className="sr-only" />
                        <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${selectedActiveFilters.length === 0 ? "bg-white text-indigo-700 border-indigo-200 ring-2 ring-indigo-100" : "bg-white/80 text-gray-500 border-gray-200 hover:bg-white"}`}>
                          すべて
                        </span>
                      </label>
                      {allActiveFilters.map((act) => {
                        const selected = selectedActiveFilters.includes(act);
                        return (
                          <label key={act} className="cursor-pointer">
                            <input type="checkbox" checked={selected} onChange={() => toggleActiveFilter(act)} className="sr-only" />
                            <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${selected ? `${getActiveBadgeClass(act)} ring-2 ring-offset-1 ring-indigo-100` : "bg-white/80 text-gray-500 border-gray-200 hover:bg-white"}`}>
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

          {error && (
            <div className="mb-4 bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">
              {error}
            </div>
          )}

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
                          aria-sort={sortKey === "name" ? (sortOrder === "asc" ? "ascending" : "descending") : "none"}
                          className="px-6 py-3 font-medium select-none"
                        >
                          <button type="button" onClick={() => handleSort("name")} className="flex items-center gap-1 cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 rounded">
                            名前{getSortIcon("name")}
                          </button>
                        </th>
                        <th className="px-6 py-3 font-medium">メールアドレス</th>
                        <th
                          aria-sort={sortKey === "role" ? (sortOrder === "asc" ? "ascending" : "descending") : "none"}
                          className="px-6 py-3 font-medium select-none"
                        >
                          <button type="button" onClick={() => handleSort("role")} className="flex items-center gap-1 cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 rounded">
                            権限{getSortIcon("role")}
                          </button>
                        </th>
                        <th
                          aria-sort={sortKey === "created_at" ? (sortOrder === "asc" ? "ascending" : "descending") : "none"}
                          className="px-6 py-3 font-medium select-none"
                        >
                          <button type="button" onClick={() => handleSort("created_at")} className="flex items-center gap-1 cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 rounded">
                            登録日時{getSortIcon("created_at")}
                          </button>
                        </th>
                        <th className="px-6 py-3 font-medium">更新日時</th>
                        <th
                          aria-sort={sortKey === "status" ? (sortOrder === "asc" ? "ascending" : "descending") : "none"}
                          className="px-6 py-3 text-right font-medium select-none normal-case"
                        >
                          <button type="button" onClick={() => handleSort("status")} className="flex items-center justify-end gap-1 w-full cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 rounded">
                            状態{getSortIcon("status")}
                          </button>
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {rows.length === 0 ? (
                        <tr>
                          <td colSpan={6} className="px-6 py-12 text-center text-gray-500">
                            スタッフが見つかりません
                          </td>
                        </tr>
                      ) : (
                        rows.map((row) => (
                          <motion.tr key={row.id} initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="border-t border-gray-200 hover:bg-gray-50">
                            <td className="px-6 py-4 font-medium text-gray-900">
                              <div className="flex min-h-[1.75rem] items-center gap-2.5 min-w-0">
                                <UserAvatar name={row.name} />
                                <span className="truncate leading-tight">{row.name}</span>
                              </div>
                            </td>
                            <td className="px-6 py-4 text-gray-600 break-all max-w-[14rem]">{row.email}</td>
                            <td className="px-6 py-4">
                              <div className="flex min-h-[1.75rem] items-center">
                                <RoleSegmentSwitch role={row.role} onChange={(r) => setRowRole(row.id, r)} ariaLabel={`${row.name}の権限`} disabled={authLoading || row.id === myStaffId} />
                              </div>
                            </td>
                            <td className="px-6 py-4 text-gray-600 whitespace-nowrap">{row.createdAt}</td>
                            <td className="px-6 py-4 text-gray-600 whitespace-nowrap">{row.updatedAt}</td>
                            <td className="px-6 py-4 text-right">
                              <div className="flex min-h-[1.75rem] items-center justify-end">
                                <ActiveSegmentSwitch active={row.active} onChange={(a) => setRowActive(row.id, a)} ariaLabel={`${row.name}の状態`} disabled={authLoading || row.id === myStaffId} />
                              </div>
                            </td>
                          </motion.tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>

                {pager && (
                  <Pager
                    pager={pager}
                    onPageChange={(p) => setCurrentPage(p)}
                    onLimitChange={(l) => { setPageSize(l); setCurrentPage(1); }}
                    limitOptions={[10, 50, 100, 250, 500]}
                  />
                )}
              </>
            )}
          </div>
        </div>
      </main>

      <ConsoleFooter />

      <AnimatePresence>
        {mutationError && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
            className="fixed bottom-6 right-6 bg-gray-900 text-white px-4 py-3 rounded-lg shadow-lg text-sm z-[60]"
          >
            {mutationError}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
