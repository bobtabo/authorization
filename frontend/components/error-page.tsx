"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { AlertTriangle, ArrowLeft, ShieldCheck } from "lucide-react";
import { getBackendConnectionDetail } from "@/lib/backend-connection-hint";
import { RUNTIME_STORAGE_KEY } from "@/src/api/client";

const ERROR_TITLE: Record<number | "default", string> = {
  400: "リクエストが不正です",
  401: "認証が必要です",
  403: "アクセスが拒否されました",
  404: "ページが見つかりませんでした",
  429: "リクエストが多すぎます",
  500: "サーバーエラーが発生しました",
  503: "サービスが利用できません",
  default: "エラーが発生しました",
};

const ERROR_MESSAGE: Record<number | "default", string> = {
  400: "入力内容に問題があります。内容を確認してから再度お試しください。",
  401: "ログインセッションが無効です。ログインページから再度サインインしてください。",
  403: "このページへのアクセス権限がありません。招待リンクから登録手続きを行ってください。",
  404: "お探しのページは移動したか、URL が間違っている可能性があります。",
  429: "短時間に多くのリクエストが送信されました。しばらく時間をおいてから再度お試しください。",
  500: "サーバーで予期しない問題が発生しました。時間をおいてから再度お試しください。",
  503: "現在メンテナンス中か、サーバーが高負荷状態です。しばらくお待ちください。",
  default: "しばらく時間をおいてから、もう一度お試しください。",
};

const RUNTIME_LABEL: Record<string, string> = {
  go:       "Go",
  kotlin:   "Kotlin",
  php:      "PHP",
  python:   "Python",
  "rb-hanami": "Ruby (Hanami)",
  "rb-rails":  "Ruby (Rails)",
  rust:     "Rust",
  ts:       "TypeScript",
};

export type ErrorPageProps = {
  /** HTTP ステータス風のコード（表示用。未指定は 404） */
  statusCode?: number;
  /** 見出し */
  title?: string;
  /** 説明文 */
  message?: string;
  /** プライマリリンク先（既定: ログイン） */
  primaryTo?: string;
  primaryLabel?: string;
  /** セカンダリ（戻る等）— 未指定なら非表示 */
  secondaryTo?: string;
  secondaryLabel?: string;
};

export function ErrorPage({
  statusCode = 404,
  title,
  message,
  primaryTo = "/login",
  primaryLabel = "ログインへ",
  secondaryTo,
  secondaryLabel = "ひとつ前へ",
}: ErrorPageProps): React.JSX.Element {
  const resolvedTitle = title ?? ERROR_TITLE[statusCode] ?? ERROR_TITLE.default;
  const resolvedMessage = message ?? ERROR_MESSAGE[statusCode] ?? ERROR_MESSAGE.default;

  const isNotFound = statusCode === 404;
  const [runtime, setRuntime] = useState<string>("php");
  const [connectionDetail, setConnectionDetail] = useState<string>("");

  useEffect(() => {
    setRuntime(localStorage.getItem(RUNTIME_STORAGE_KEY) ?? "php");
    setConnectionDetail(getBackendConnectionDetail());
  }, []);

  const runtimeLabel = RUNTIME_LABEL[runtime] ?? runtime;

  return (
    <div className="flex min-h-screen flex-col bg-[#f6f8fa]">
      <div className="shrink-0 border-b border-[#d0d7de] bg-white px-4 py-3 text-left shadow-sm">
        <p className="text-xs font-semibold text-[#1f2328]" suppressHydrationWarning>
          Backend は {runtimeLabel} と通信しています
        </p>
        <p
          className="mt-1.5 break-all font-mono text-[11px] leading-relaxed text-[#656d76]"
          title={connectionDetail}
          suppressHydrationWarning
        >
          {connectionDetail}
        </p>
      </div>

      <main className="flex flex-1 flex-col items-center justify-center px-4 py-12">
        <div className="flex w-full max-w-[400px] flex-col items-center">
          {/* ログイン画面と同系のブランド */}
          <div
            className="mb-5 flex h-14 w-14 items-center justify-center rounded-xl bg-indigo-600 text-white shadow-md ring-1 ring-black/5"
            aria-hidden
          >
            <ShieldCheck className="h-8 w-8" strokeWidth={1.75} />
          </div>

          <h1 className="text-center text-2xl font-semibold tracking-tight text-[#1f2328]">
            Authorization Gateway
          </h1>

          <div className="mt-6 w-full rounded-xl border border-[#d0d7de] bg-white p-6 shadow-sm">
            <div className="flex flex-col items-center text-center">
              <div
                className={`mb-5 flex w-full max-w-[16rem] items-center justify-center rounded-xl py-6 sm:max-w-[18rem] sm:py-7 ${
                  isNotFound
                    ? "bg-slate-100 text-slate-700"
                    : "bg-amber-50 text-amber-900 ring-1 ring-amber-200/80"
                }`}
                aria-hidden
              >
                {isNotFound ? (
                  <span className="text-6xl font-bold tabular-nums tracking-tight sm:text-7xl">
                    404
                  </span>
                ) : (
                  <div className="flex flex-col items-center gap-2.5">
                    <span className="text-5xl font-bold tabular-nums tracking-tight sm:text-6xl">
                      {statusCode}
                    </span>
                    <AlertTriangle className="h-8 w-8 text-amber-700 sm:h-9 sm:w-9" strokeWidth={1.75} />
                  </div>
                )}
              </div>

              <h2 className="text-lg font-semibold leading-snug text-[#1f2328] sm:text-xl">
                {resolvedTitle}
              </h2>
              <p className="mt-2 text-sm leading-relaxed text-[#656d76]">{resolvedMessage}</p>

              <div className="mt-6 flex w-full flex-col gap-2.5 sm:flex-row sm:justify-center">
                <Link
                  href={primaryTo}
                  className="inline-flex items-center justify-center rounded-md bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white shadow-sm transition hover:bg-indigo-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:ring-offset-2"
                >
                  {primaryLabel}
                </Link>
                {secondaryTo ? (
                  <Link
                    href={secondaryTo}
                    className="inline-flex items-center justify-center gap-1.5 rounded-md border border-[#d0d7de] bg-white px-4 py-2.5 text-sm font-medium text-[#24292f] shadow-sm transition hover:bg-gray-50 hover:border-[#b6bcc3]"
                  >
                    <ArrowLeft className="h-4 w-4" aria-hidden />
                    {secondaryLabel}
                  </Link>
                ) : null}
              </div>
            </div>
          </div>
        </div>
      </main>

      <footer className="border-t border-[#d0d7de] bg-white py-5 text-center text-xs text-[#656d76]">
        © 2026 Authorization Gateway. All rights reserved.
      </footer>
    </div>
  );
}
