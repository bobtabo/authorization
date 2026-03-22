"use client";

import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Building2, ArrowLeft, X, Trash2, Play } from "lucide-react";
import { ConsoleHeader } from "@/components/console-header";

/** モック: 詳細取得想定の初期データ（必須系は入力済み） */
const DEFAULT_DETAIL = {
  clientName: "株式会社ストラテジックインサイト",
  postalCode: "1070061",
  prefecture: "東京都",
  city: "港区北青山",
  street: "二丁目5番8号",
  building: "青山OMスクエア",
  tel: "0312345678",
  email: "contact@sii-japan.co.jp",
  status: "準備中" as const,
  startedAt: "—",
  stoppedAt: "—",
  createdAt: "2026-01-15 09:30",
  updatedAt: "2026-01-20 10:00",
};

function getStatusStyle(status: string) {
  switch (status) {
    case "有効":
      return "bg-emerald-100 text-emerald-800 border border-emerald-200";
    case "無効":
      return "bg-slate-100 text-slate-600 border border-slate-200";
    case "停止":
      return "bg-rose-100 text-rose-700 border border-rose-200";
    case "準備中":
      return "bg-amber-100 text-amber-700 border border-amber-200";
    default:
      return "bg-gray-100 text-gray-700 border border-gray-200";
  }
}

function DetailRow({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div className="grid grid-cols-1 gap-1 sm:grid-cols-[11rem_1fr] sm:gap-6 py-4 border-b border-gray-100 last:border-b-0">
      <dt className="text-sm font-medium text-gray-500 shrink-0">{label}</dt>
      <dd className="text-sm text-gray-900 min-w-0 break-words">{children}</dd>
    </div>
  );
}

export default function ClientShowPage(): React.JSX.Element {
  const [data] = useState(DEFAULT_DETAIL);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [startOpen, setStartOpen] = useState(false);
  const [starting, setStarting] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 3000);
    return () => clearTimeout(t);
  }, [toast]);

  const handleDelete = () => {
    setDeleting(true);
    setTimeout(() => {
      setDeleting(false);
      setDeleteOpen(false);
      setToast("削除しました（モック）");
      setTimeout(() => {
        window.location.href = "/clients";
      }, 800);
    }, 600);
  };

  const handleStart = () => {
    setStarting(true);
    setTimeout(() => {
      setStarting(false);
      setStartOpen(false);
      setToast("利用を開始しました（モック）");
    }, 600);
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <ConsoleHeader />

      <main className="flex-1">
        <div className="max-w-4xl mx-auto px-6 py-10">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-2xl font-semibold text-gray-900 flex items-center gap-2">
              <Building2 size={24} />
              クライアント詳細
            </h1>
            <a
              href="/clients"
              className="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-800"
            >
              <ArrowLeft size={16} />
              一覧へ戻る
            </a>
          </div>

          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden"
          >
            <div className="px-6 pt-6 pb-2">
              <div className="flex flex-wrap items-center gap-3 mb-2">
                <h2 className="text-lg font-semibold text-gray-900">
                  {data.clientName}
                </h2>
                <span
                  className={`inline-block px-2.5 py-1 rounded-full text-xs font-medium ${getStatusStyle(
                    data.status
                  )}`}
                >
                  {data.status}
                </span>
              </div>
            </div>

            <div className="px-6 pb-6">
              <dl>
                <DetailRow label="郵便番号">〒{data.postalCode}</DetailRow>
                <DetailRow label="都道府県">{data.prefecture}</DetailRow>
                <DetailRow label="市区町村">{data.city}</DetailRow>
                <DetailRow label="丁目・番地">{data.street}</DetailRow>
                <DetailRow label="ビル名">{data.building}</DetailRow>
                <DetailRow label="電話番号">{data.tel}</DetailRow>
                <DetailRow label="メールアドレス">
                  <a
                    href={`mailto:${data.email}`}
                    className="text-indigo-600 hover:text-indigo-700 hover:underline"
                  >
                    {data.email}
                  </a>
                </DetailRow>
                <DetailRow label="利用開始日時">{data.startedAt}</DetailRow>
                <DetailRow label="利用停止日時">{data.stoppedAt}</DetailRow>
                <DetailRow label="登録日時">{data.createdAt}</DetailRow>
                <DetailRow label="更新日時">{data.updatedAt}</DetailRow>
              </dl>
            </div>

            <div className="px-6 py-5 border-t border-gray-200 bg-gray-50/80">
              <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                <button
                  type="button"
                  onClick={() => setDeleteOpen(true)}
                  className="inline-flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium rounded-lg border border-red-200 bg-white text-red-700 hover:bg-red-50 hover:border-red-300 transition-colors w-full md:w-auto shrink-0"
                >
                  <Trash2 size={16} />
                  削除
                </button>

                <div className="hidden md:block flex-1 min-w-[2rem]" aria-hidden />

                <div className="flex flex-wrap items-center justify-end gap-3 w-full md:w-auto">
                  <a
                    href="/clients"
                    className="inline-flex items-center justify-center px-4 py-2.5 text-sm font-medium rounded-lg border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 hover:border-gray-400 transition-colors"
                  >
                    キャンセル
                  </a>
                  <button
                    type="button"
                    onClick={() => setStartOpen(true)}
                    className="inline-flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition-colors"
                  >
                    <Play size={16} />
                    利用開始
                  </button>
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      </main>

      {/* 削除確認 */}
      <AnimatePresence>
        {deleteOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
            onClick={() => !deleting && setDeleteOpen(false)}
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
                  type="button"
                  onClick={() => !deleting && setDeleteOpen(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <X size={20} />
                </button>
              </div>
              <p className="text-gray-600 mb-6">
                「{data.clientName}」を削除してもよろしいですか？この操作は取り消せません。
              </p>
              <div className="flex gap-3 justify-end">
                <button
                  type="button"
                  onClick={() => setDeleteOpen(false)}
                  disabled={deleting}
                  className="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                >
                  キャンセル
                </button>
                <button
                  type="button"
                  onClick={handleDelete}
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

      {/* 利用開始確認 */}
      <AnimatePresence>
        {startOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
            onClick={() => !starting && setStartOpen(false)}
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
                  利用開始の確認
                </h2>
                <button
                  type="button"
                  onClick={() => !starting && setStartOpen(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <X size={20} />
                </button>
              </div>
              <p className="text-gray-600 mb-6">
                「{data.clientName}」の利用を開始してもよろしいですか？
              </p>
              <div className="flex gap-3 justify-end">
                <button
                  type="button"
                  onClick={() => setStartOpen(false)}
                  disabled={starting}
                  className="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                >
                  キャンセル
                </button>
                <button
                  type="button"
                  onClick={handleStart}
                  disabled={starting}
                  className="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 disabled:opacity-50 flex items-center gap-2"
                >
                  {starting && (
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  )}
                  利用を開始する
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
            className="fixed bottom-6 right-6 bg-gray-900 text-white px-4 py-3 rounded-lg shadow-lg text-sm z-[60]"
          >
            {toast}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
