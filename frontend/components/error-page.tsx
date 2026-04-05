"use client";

import React from "react";
import { Link } from "react-router-dom";
import { AlertTriangle, ArrowLeft, ShieldCheck } from "lucide-react";

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
  const resolvedTitle =
    title ??
    (statusCode === 404 ? "ページが見つかりませんでした" : "エラーが発生しました");
  const resolvedMessage =
    message ??
    (statusCode === 404
      ? "お探しのページは移動したか、URL が間違っている可能性があります。"
      : "しばらく時間をおいてから、もう一度お試しください。");

  const isNotFound = statusCode === 404;

  return (
    <div className="flex min-h-screen flex-col bg-[#f6f8fa]">
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
            Authorization Console
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
                  to={primaryTo}
                  className="inline-flex items-center justify-center rounded-md bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white shadow-sm transition hover:bg-indigo-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:ring-offset-2"
                >
                  {primaryLabel}
                </Link>
                {secondaryTo ? (
                  <Link
                    to={secondaryTo}
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
        © 2026 Authorization Console. All rights reserved.
      </footer>
    </div>
  );
}
