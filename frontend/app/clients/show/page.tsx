"use client";

import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Building2, ArrowLeft, X, Trash2, Play, Square, History, Copy, Check, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-react";
import { ConsoleHeader } from "@/components/console-header";
import { ConsoleFooter } from "@/components/console-footer";
import { getClient, updateClient, deleteClient, getJwtHistories, type JwtHistory } from "@/src/api/clients";
import { formatTimestamp } from "@/lib/format-datetime";
import { extractApiError } from "@/lib/api-error";

type ClientStatus = "準備中" | "利用中" | "停止中" | "アーカイブ";

type ClientDetail = {
  clientName: string;
  identifier: string;
  postalCode: string;
  prefecture: string;
  city: string;
  street: string;
  building: string;
  tel: string;
  email: string;
  status: ClientStatus;
  startedAt: string;
  stoppedAt: string;
  createdAt: string;
  updatedAt: string;
};

function getStatusStyle(status: string) {
  switch (status) {
    case "利用中":
      return "bg-emerald-100 text-emerald-800 border border-emerald-200";
    case "アーカイブ":
      return "bg-slate-100 text-slate-600 border border-slate-200";
    case "停止中":
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

const STATUS_MAP: Record<number, ClientStatus> = { 1: "準備中", 2: "利用中", 3: "停止中", 4: "アーカイブ" };

function JwtCell({ jwt }: { jwt: string }) {
  const [copied, setCopied] = useState(false);
  const [tooltip, setTooltip] = useState<{ x: number; y: number } | null>(null);

  const handleCopy = () => {
    navigator.clipboard.writeText(jwt).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
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
        title="JWTをコピー"
      >
        {copied ? <Check size={14} className="text-emerald-500" /> : <Copy size={14} />}
      </button>
    </div>
  );
}

export default function ClientShowPage(): React.JSX.Element {
  const [clientId, setClientId] = useState<number | null>(null);
  const [detail, setDetail] = useState<ClientDetail | null>(null);
  const [version, setVersion] = useState<number>(1);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [startOpen, setStartOpen] = useState(false);
  const [starting, setStarting] = useState(false);
  const [stopOpen, setStopOpen] = useState(false);
  const [stopping, setStopping] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const mockHistories: JwtHistory[] = [
    {
      id: 1,
      member_id: "M081916",
      issue_at: "2026-05-22 20:17:00",
      jwt: "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlNIQTI1NjpUdzAyTENvYVJLdnNvMHA5WUttMzdOUmd6TldxaUJDem4vM0tQT3o0MU5vIn0.eyJpc3MiOiJhdXRob3JpemF0aW9uIiwic3ViIjoiTTA4MTkxNiIsImF1ZCI6ImFscGhhLXRlY2giLCJleHAiOjE3NzkzOTE2MTYsImlhdCI6MTc3OTM4OTgxNiwibmJmIjoxNzc5Mzg5ODE2LCJqdGkiOiI3MWZlYmExZi1mNmYxLTQ0MzEtODE0Mi04NzRmZDViMGJkNzAifQ.UVypBFc4r7UeRJlMfHYQLB2w80XD1yd0hNU0rvxUsBAi9-q1bpRKm6mdDHhVuZe0Z3VhMcxJWfecGiAiRGoZSQmjbMnohBFMJR-9h0R_x-yweHy-g_YSuOUSc7PWbbPUHUYtgvkDU9whkhaphepBFMONST_pgSM0owEGRDDhm-MlkxnnWqLDJH4Nx9Qi-dDMRzXEVYiCYCABL-RdeMh_Kw_ZXdLYLLPAVHczU2c0PfZUmEqyr0QWy9WR3rpaVy9vHNDSfY4AG54ZL6B4sE-ZvHisepb2Fg2BRS-HI8RH1AKOVHUHW4kdtLErQfa0ANv6G-NPt9zP09FtBng8rqnRU9L-9b3_q6gQ9NrqmcAuV3P3qfh6UKKhh2gq4_Q-l6oYQlg4jOpiEBaz3X3UWPYEUZ4lCfA6OFfi45jlPVlRZwxTijC8rnFwOF-J3A2QZ2k2Uv4L7zD8uvHTVmzPEIwaZ6DQMX4n5EYje3seW8OIT3O3a2fBMhY205L3D_zJIVJFqWk83URwGz7JEF8Smgj4NxzQ0laKJawVu1py0H4d9j75fKOB5YXRtj3FJ6l4ai_JKFLnFfwo9dCYbXA2syV8-4DYAk5-FeFFigIgGrpEJJDHOGN9bA0PukL-iwXyT6SCyAZz1g4voSpz5v8r9O5kQRkFndaIQqQMNdSmXbAmfcY",
    },
  ];
  const [histories, setHistories] = useState<JwtHistory[]>(mockHistories);
  const [activeTab, setActiveTab] = useState<"info" | "history">("info");
  const [historyPage, setHistoryPage] = useState(1);
  const [historyPageSize, setHistoryPageSize] = useState(10);

  const loadDetail = (id: number | string) => {
    return getClient(id).then((res) => {
      const d = res as Record<string, unknown>;
      setDetail({
        clientName: d.name as string,
        identifier: d.identifier as string ?? "",
        postalCode: d.post_code as string ?? "",
        prefecture: d.pref as string ?? "",
        city: d.city as string ?? "",
        street: d.address as string ?? "",
        building: d.building as string ?? "",
        tel: d.tel as string ?? "",
        email: d.email as string ?? "",
        status: STATUS_MAP[d.status as number] ?? "準備中",
        startedAt: formatTimestamp(d.start_at as string | null),
        stoppedAt: formatTimestamp(d.stop_at as string | null),
        createdAt: formatTimestamp(d.created_at as string),
        updatedAt: formatTimestamp(d.updated_at as string),
      });
      setVersion((d.version as number) ?? 1);
    });
  };

  useEffect(() => {
    const id = new URLSearchParams(window.location.search).get("id");
    if (!id) return;
    setClientId(Number(id));
    loadDetail(id);
    getJwtHistories(id).then(setHistories).catch(() => {});
  }, []);

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 3000);
    return () => clearTimeout(t);
  }, [toast]);

  const buildUpdatePayload = (status: number) => ({
    id: clientId,
    name: detail?.clientName,
    post_code: detail?.postalCode,
    pref: detail?.prefecture,
    city: detail?.city,
    address: detail?.street,
    building: detail?.building,
    tel: detail?.tel,
    email: detail?.email,
    status,
    version,
  });

  const handleDelete = () => {
    if (!clientId) return;
    setDeleting(true);
    deleteClient(clientId, { version }).then(() => {
      window.location.href = "/clients";
    }).catch((err: unknown) => {
      setDeleting(false);
      setDeleteOpen(false);
      setToast(extractApiError(err, "削除に失敗しました。"));
    });
  };

  const handleStart = () => {
    if (!clientId || !detail) return;
    setStarting(true);
    updateClient(clientId, buildUpdatePayload(2))
      .then(() => loadDetail(clientId))
      .then(() => {
        setStarting(false);
        setStartOpen(false);
        setToast("利用を開始しました。");
      }).catch((err: unknown) => {
        setStarting(false);
        setToast(extractApiError(err, "利用開始に失敗しました。"));
      });
  };

  const handleStop = () => {
    if (!clientId || !detail) return;
    setStopping(true);
    updateClient(clientId, buildUpdatePayload(3))
      .then(() => loadDetail(clientId))
      .then(() => {
        setStopping(false);
        setStopOpen(false);
        setToast("利用を停止しました。");
      }).catch((err: unknown) => {
        setStopping(false);
        setToast(extractApiError(err, "利用停止に失敗しました。"));
      });
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

          {!detail ? (
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
              ) : (() => {
                const totalHistoryPages = Math.max(1, Math.ceil(histories.length / historyPageSize));
                const safHistoryPage = Math.min(historyPage, totalHistoryPages);
                const historyStart = (safHistoryPage - 1) * historyPageSize;
                const pagedHistories = histories.slice(historyStart, historyStart + historyPageSize);
                return (
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
                          {histories.length === 0 ? (
                            <tr>
                              <td colSpan={3} className="px-6 py-10 text-center text-sm text-gray-400">発行履歴はありません</td>
                            </tr>
                          ) : (
                            pagedHistories.map((h) => (
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

                    <div className="border-t border-gray-200 px-6 py-3 flex items-center justify-between bg-gray-50/80">
                      <div className="text-xs text-gray-500">
                        全 {histories.length} 件中 {histories.length === 0 ? 0 : historyStart + 1}–{Math.min(historyStart + historyPageSize, histories.length)} 件表示
                      </div>
                      <div className="flex items-center gap-2 text-gray-700">
                        <select
                          value={historyPageSize}
                          onChange={(e) => { setHistoryPageSize(Number(e.target.value)); setHistoryPage(1); }}
                          className="border border-gray-300 bg-white rounded-md px-2 py-1 text-xs text-gray-700"
                        >
                          <option value={10}>10件</option>
                          <option value={20}>20件</option>
                          <option value={50}>50件</option>
                        </select>
                        <button disabled={safHistoryPage === 1} onClick={() => setHistoryPage(1)}
                          className="p-1 rounded border border-gray-300 bg-white text-gray-600 hover:border-indigo-300 hover:text-indigo-600 hover:bg-indigo-50 transition-colors disabled:opacity-35 disabled:hover:border-gray-300 disabled:hover:text-gray-600 disabled:hover:bg-white">
                          <ChevronsLeft size={14} />
                        </button>
                        <button disabled={safHistoryPage === 1} onClick={() => setHistoryPage((p) => Math.max(1, p - 1))}
                          className="p-1 rounded border border-gray-300 bg-white text-gray-600 hover:border-indigo-300 hover:text-indigo-600 hover:bg-indigo-50 transition-colors disabled:opacity-35 disabled:hover:border-gray-300 disabled:hover:text-gray-600 disabled:hover:bg-white">
                          <ChevronLeft size={14} />
                        </button>
                        <span className="text-xs px-1 font-medium text-gray-700">{safHistoryPage} / {totalHistoryPages}</span>
                        <button disabled={safHistoryPage === totalHistoryPages} onClick={() => setHistoryPage((p) => Math.min(totalHistoryPages, p + 1))}
                          className="p-1 rounded border border-gray-300 bg-white text-gray-600 hover:border-indigo-300 hover:text-indigo-600 hover:bg-indigo-50 transition-colors disabled:opacity-35 disabled:hover:border-gray-300 disabled:hover:text-gray-600 disabled:hover:bg-white">
                          <ChevronRight size={14} />
                        </button>
                        <button disabled={safHistoryPage === totalHistoryPages} onClick={() => setHistoryPage(totalHistoryPages)}
                          className="p-1 rounded border border-gray-300 bg-white text-gray-600 hover:border-indigo-300 hover:text-indigo-600 hover:bg-indigo-50 transition-colors disabled:opacity-35 disabled:hover:border-gray-300 disabled:hover:text-gray-600 disabled:hover:bg-white">
                          <ChevronsRight size={14} />
                        </button>
                      </div>
                    </div>
                  </>
                );
              })()}
            </motion.div>
          )}
        </div>
      </main>

      <ConsoleFooter />

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
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 利用停止確認 */}
      <AnimatePresence>
        {stopOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
            onClick={() => !stopping && setStopOpen(false)}
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
                  利用停止の確認
                </h2>
                <button
                  type="button"
                  onClick={() => !stopping && setStopOpen(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <X size={20} />
                </button>
              </div>
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
