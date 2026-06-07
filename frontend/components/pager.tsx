"use client";

import React from "react";
import { ChevronsLeft, ChevronLeft, ChevronRight, ChevronsRight } from "lucide-react";
import type { Pager as PagerData } from "@/src/api/clients";

type Props = {
  pager: PagerData;
  onPageChange: (page: number) => void;
  onLimitChange: (limit: number) => void;
  limitOptions?: number[];
};

const NAV_BTN =
  "p-1.5 rounded-md border border-gray-300 bg-white text-gray-700 hover:border-indigo-300 hover:text-indigo-700 hover:bg-indigo-50 transition-colors disabled:opacity-35 disabled:pointer-events-none";

export function Pager({ pager, onPageChange, onLimitChange, limitOptions = [10, 50, 100] }: Props): React.JSX.Element {
  const pages: number[] = [];
  for (let p = pager.startPage; p <= pager.endPage; p++) pages.push(p);

  return (
    <div className="border-t border-gray-200 px-6 py-4 flex items-center justify-between bg-gray-50">
      <div className="text-sm text-gray-600">
        {pager.count === 0
          ? "0件"
          : `${pager.firstRecordCount}件目〜${pager.lastRecordCount}件目（全${pager.count}件）`}
      </div>

      <div className="flex items-center gap-1 text-gray-700">
        <select
          value={pager.limit}
          onChange={(e) => onLimitChange(Number(e.target.value))}
          className="mr-2 border border-gray-300 bg-white rounded-md px-2 py-1 text-sm text-gray-700"
        >
          {limitOptions.map((n) => (
            <option key={n} value={n}>{n}件</option>
          ))}
        </select>

        <button disabled={!pager.previous} onClick={() => onPageChange(1)} className={NAV_BTN}>
          <ChevronsLeft size={16} />
        </button>
        <button disabled={!pager.previous} onClick={() => onPageChange(pager.page - 1)} className={NAV_BTN}>
          <ChevronLeft size={16} />
        </button>

        {pager.startPage > 1 && (
          <>
            <button
              onClick={() => onPageChange(1)}
              className="min-w-[2rem] px-2 py-1.5 rounded-md border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:border-indigo-300 hover:text-indigo-700 hover:bg-indigo-50 transition-colors"
            >
              1
            </button>
            <span className="px-0.5 text-gray-400 text-sm select-none">…</span>
          </>
        )}

        {pages.map((p) => (
          <button
            key={p}
            onClick={() => onPageChange(p)}
            className={`min-w-[2rem] px-2 py-1.5 rounded-md border text-sm font-medium transition-colors ${
              p === pager.page
                ? "border-indigo-500 bg-indigo-600 text-white"
                : "border-gray-300 bg-white text-gray-700 hover:border-indigo-300 hover:text-indigo-700 hover:bg-indigo-50"
            }`}
          >
            {p}
          </button>
        ))}

        {pager.endPage < pager.pageCount && (
          <>
            <span className="px-0.5 text-gray-400 text-sm select-none">…</span>
            <button
              onClick={() => onPageChange(pager.pageCount)}
              className="min-w-[2rem] px-2 py-1.5 rounded-md border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:border-indigo-300 hover:text-indigo-700 hover:bg-indigo-50 transition-colors"
            >
              {pager.pageCount}
            </button>
          </>
        )}

        <button disabled={!pager.next} onClick={() => onPageChange(pager.page + 1)} className={NAV_BTN}>
          <ChevronRight size={16} />
        </button>
        <button disabled={!pager.next} onClick={() => onPageChange(pager.pageCount)} className={NAV_BTN}>
          <ChevronsRight size={16} />
        </button>
      </div>
    </div>
  );
}
