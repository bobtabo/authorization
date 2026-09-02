"use client";

import { motion, AnimatePresence } from "framer-motion";
import { Building2, ArrowLeft, Trash2, Play, Square, History, Copy, Check, AlertCircle } from "lucide-react";
import React, { useEffect, useState } from "react";

import { ConfirmDialog } from "@/shared/components/confirm-dialog";
import { ConsoleFooter } from "@/shared/components/console-footer";
import { ConsoleHeader } from "@/shared/components/console-header";
import { Pager } from "@/shared/components/pager";
import { formatTimestamp } from "@/shared/lib/format-datetime";

import { useClientDetail } from "../hooks/useClientDetail";

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

function JwtCell({ jwt }: { jwt: string }) {
  const [copyStatus, setCopyStatus] = useState<"idle" | "copied" | "failed">("idle");
  const [tooltip, setTooltip] = useState<{ x: number; y: number } | null>(null);

  useEffect(() => {
    if (copyStatus === "idle") return;
    const t = setTimeout(() => setCopyStatus("idle"), 2000);
    return () => clearTimeout(t);
  }, [copyStatus]);

  const handleCopy = () => {
    navigator.clipboard.writeText(jwt).then(() => {
      setCopyStatus("copied");
    }).catch(() => {
      setCopyStatus("failed");
    });
  };

  const handleMouseEnter = (e: React.MouseEvent) => {
    const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
    setTooltip({ x: rect.left, y: rect.top - 6 });
  };

  return (
    <div className="flex items-center gap-2 min-w-0">
      <span
        className="font-mono text-xs text-gray-500 cursor-default"
        onMouseEnter={handleMouseEnter}
        onMouseLeave={() => setTooltip(null)}
      >
        {jwt.slice(0, 40)}…
      </span>
      {tooltip && (
        <div
          className="fixed z-50 max-w-lg bg-white text-gray-700 text-xs font-mono rounded-lg px-3 py-2 shadow-lg border border-gray-200 break-all leading-relaxed pointer-events-none"
          style={{ left: tooltip.x, top: tooltip.y, transform: "translateY(-100%)" }}
        >
          {jwt}
        </div>
      )}
      <button
        type="button"
        onClick={handleCopy}
        className="shrink-0 text-gray-400 hover:text-gray-600 transition-colors"
        title={copyStatus === "failed" ? "コピーに失敗しました" : "JWTをコピー"}
      >
        {copyStatus === "failed" ? (
          <AlertCircle size={14} className="text-red-500" />
        ) : copyStatus === "copied" ? (
          <Check size={14} className="text-emerald-500" />
        ) : (
          <Copy size={14} />
        )}
      </button>
    </div>
  );
}

export function ClientDetail(): React.JSX.Element {
  const {
    detail,
    loadError,
    deleteOpen,
    setDeleteOpen,
    deleting,
    startOpen,
    setStartOpen,
    starting,
    stopOpen,
    setStopOpen,
    stopping,
    toast,
    jwtData,
    jwtPager,
    activeTab,
    setActiveTab,
    setHistoryPage,
    setHistoryPageSize,
    handleDelete,
    handleStart,
    handleStop,
    getStatusStyle,
  } = useClientDetail();

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

          {loadError ? (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">
              {loadError}
            </div>
          ) : !detail ? (
            <div className="flex items-center justify-center py-20">
              <div className="w-8 h-8 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : (
            <motion.div
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden"
            >
              {/* ヘッダー */}
              <div className="px-6 pt-6 pb-2">
                <div className="flex flex-wrap items-center gap-3 mb-1">
                  <h2 className="text-lg font-semibold text-gray-900">
                    {detail.clientName}
                  </h2>
                  <span
                    className={`inline-block px-2.5 py-1 rounded-full text-xs font-medium ${getStatusStyle(
                      detail.status
                    )}`}
                  >
                    {detail.status}
                  </span>
                </div>
                <p className="text-xs text-gray-400 mb-2">
                  <span className="font-medium text-gray-400">識別名：</span>
                  <span className="font-mono">{detail.identifier}</span>
                </p>
              </div>

              {/* タブバー */}
              <div className="px-6 flex border-b border-gray-200">
                <button
                  type="button"
                  onClick={() => setActiveTab("info")}
                  className={`px-4 py-3 text-sm font-medium transition-colors border-b-2 -mb-px ${
                    activeTab === "info"
                      ? "border-indigo-600 text-indigo-600"
                      : "border-transparent text-gray-500 hover:text-gray-700"
                  }`}
                >
                  基本情報
                </button>
                <button
                  type="button"
                  onClick={() => setActiveTab("history")}
                  className={`px-4 py-3 text-sm font-medium transition-colors border-b-2 -mb-px flex items-center gap-1.5 ${
                    activeTab === "history"
                      ? "border-indigo-600 text-indigo-600"
                      : "border-transparent text-gray-500 hover:text-gray-700"
                  }`}
                >
                  <History size={14} />
                  JWT履歴
                </button>
              </div>

              {/* タブコンテンツ */}
              {activeTab === "info" ? (
                <>
                  <div className="px-6 pb-6">
                    <dl>
                      <DetailRow label="郵便番号">〒{detail.postalCode}</DetailRow>
                      <DetailRow label="都道府県">{detail.prefecture}</DetailRow>
                      <DetailRow label="市区町村">{detail.city}</DetailRow>
                      <DetailRow label="丁目・番地">{detail.street}</DetailRow>
                      <DetailRow label="ビル名">{detail.building}</DetailRow>
                      <DetailRow label="電話番号">{detail.tel}</DetailRow>
                      <DetailRow label="メールアドレス">
                        <a
                          href={`mailto:${detail.email}`}
                          className="text-indigo-600 hover:text-indigo-700 hover:underline"
                        >
                          {detail.email}
                        </a>
                      </DetailRow>
                      <DetailRow label="利用開始日時">{detail.startedAt}</DetailRow>
                      <DetailRow label="利用停止日時">{detail.stoppedAt}</DetailRow>
                      <DetailRow label="登録日時">{detail.createdAt}</DetailRow>
                      <DetailRow label="更新日時">{detail.updatedAt}</DetailRow>
                    </dl>
                  </div>

                  <div className="px-6 py-5 border-t border-gray-200 bg-gray-50/80">
                    <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                      {detail.status !== "アーカイブ" && (
                        <button
                          type="button"
                          onClick={() => setDeleteOpen(true)}
                          className="inline-flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium rounded-lg border-2 border-red-500/80 bg-white text-red-600 hover:bg-red-50 hover:border-red-600 transition-colors w-full md:w-auto shrink-0"
                        >
                          <Trash2 size={16} />
                          削除
                        </button>
                      )}

                      <div className="hidden md:block flex-1 min-w-[2rem]" aria-hidden />

                      <div className="flex flex-wrap items-center justify-end gap-3 w-full md:w-auto">
                        <a
                          href="/clients"
                          className="inline-flex items-center justify-center px-4 py-2.5 text-sm font-medium rounded-lg border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 hover:border-gray-400 transition-colors"
                        >
                          キャンセル
                        </a>
                        {detail.status !== "アーカイブ" && (
                          detail.status === "利用中" ? (
                            <button
                              type="button"
                              onClick={() => setStopOpen(true)}
                              className="inline-flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium rounded-lg bg-gray-600 text-white hover:bg-gray-700 transition-colors shadow-sm"
                            >
                              <Square size={16} />
                              利用停止
                            </button>
                          ) : (
                            <button
                              type="button"
                              onClick={() => setStartOpen(true)}
                              className="inline-flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition-colors"
                            >
                              <Play size={16} />
                              利用開始
                            </button>
                          )
                        )}
                      </div>
                    </div>
                  </div>
                </>
              ) : (
                  <>
                    <div className="overflow-x-auto">
                      <table className="w-full text-sm">
                        <thead>
                          <tr className="border-b border-gray-100 bg-gray-50/60">
                            <th className="text-left px-6 py-3 font-medium text-gray-500 whitespace-nowrap">発行日時</th>
                            <th className="text-left px-6 py-3 font-medium text-gray-500 whitespace-nowrap">会員ID</th>
                            <th className="text-left px-6 py-3 font-medium text-gray-500">JWT</th>
                          </tr>
                        </thead>
                        <tbody>
                          {jwtData.length === 0 ? (
                            <tr>
                              <td colSpan={3} className="px-6 py-10 text-center text-sm text-gray-400">発行履歴はありません</td>
                            </tr>
                          ) : (
                            jwtData.map((h) => (
                              <tr key={h.id} className="border-b border-gray-100 last:border-b-0 hover:bg-gray-50/50 transition-colors">
                                <td className="px-6 py-3 whitespace-nowrap text-gray-700">{formatTimestamp(h.issue_at)}</td>
                                <td className="px-6 py-3 whitespace-nowrap text-gray-700 font-mono">{h.member_id}</td>
                                <td className="px-6 py-3"><JwtCell jwt={h.jwt} /></td>
                              </tr>
                            ))
                          )}
                        </tbody>
                      </table>
                    </div>

                    {jwtPager && (
                      <Pager
                        pager={jwtPager}
                        onPageChange={(p) => setHistoryPage(p)}
                        onLimitChange={(l) => { setHistoryPageSize(l); setHistoryPage(1); }}
                        limitOptions={[10, 50, 100, 250, 500]}
                      />
                    )}
                  </>
              )}
            </motion.div>
          )}
        </div>
      </main>

      <ConsoleFooter />

      {/* 削除確認 */}
      <ConfirmDialog
        open={deleteOpen}
        titleId="client-delete-confirm-title"
        title="削除の確認"
        onClose={() => setDeleteOpen(false)}
        closeDisabled={deleting}
      >
        <p className="text-gray-600 mb-6">
          「{detail?.clientName}」を削除してもよろしいですか？この操作は取り消せません。
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
      </ConfirmDialog>

      {/* 利用開始確認 */}
      <ConfirmDialog
        open={startOpen}
        titleId="client-start-confirm-title"
        title="利用開始の確認"
        onClose={() => setStartOpen(false)}
        closeDisabled={starting}
      >
        <p className="text-gray-600 mb-6">
          「{detail?.clientName}」の利用を開始してもよろしいですか？
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
      </ConfirmDialog>

      {/* 利用停止確認 */}
      <ConfirmDialog
        open={stopOpen}
        titleId="client-stop-confirm-title"
        title="利用停止の確認"
        onClose={() => setStopOpen(false)}
        closeDisabled={stopping}
      >
        <p className="text-gray-600 mb-6">
          「{detail?.clientName}」の利用を停止してもよろしいですか？
        </p>
        <div className="flex gap-3 justify-end">
          <button
            type="button"
            onClick={() => setStopOpen(false)}
            disabled={stopping}
            className="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            キャンセル
          </button>
          <button
            type="button"
            onClick={handleStop}
            disabled={stopping}
            className="px-4 py-2 bg-gray-600 text-white text-sm rounded-lg hover:bg-gray-700 disabled:opacity-50 flex items-center gap-2"
          >
            {stopping && (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            )}
            利用を停止する
          </button>
        </div>
      </ConfirmDialog>

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
