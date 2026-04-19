"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { ShieldCheck } from "lucide-react";
import { getBackendConnectionDetail } from "@/lib/backend-connection-hint";
import { RUNTIME_STORAGE_KEY } from "@/src/api/client";

const RUNTIME_LABEL: Record<string, string> = {
  php:    "PHP",
  go:     "Go",
  python: "Python",
  ts:     "TypeScript",
};

export default function LoginPage(): React.JSX.Element {
  const router = useRouter();
  const e2eLogin = process.env.NEXT_PUBLIC_E2E === "1";
  const [runtime, setRuntime] = useState<string>("php");
  const [connectionDetail, setConnectionDetail] = useState<string>("");

  useEffect(() => {
    const stored = localStorage.getItem(RUNTIME_STORAGE_KEY) ?? "php";
    setRuntime(stored);
    setConnectionDetail(getBackendConnectionDetail());
  }, []);

  const runtimeLabel = RUNTIME_LABEL[runtime] ?? runtime;

  return (
    <div className="min-h-screen flex flex-col bg-[#f6f8fa]">
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
        <div className="flex w-full max-w-[340px] flex-col items-center">
          <div
            className="mb-6 flex h-14 w-14 items-center justify-center rounded-xl bg-indigo-600 text-white shadow-md ring-1 ring-black/5"
            aria-hidden
          >
            <ShieldCheck className="h-8 w-8" strokeWidth={1.75} />
          </div>

          <h1 className="text-center">
            <span className="block text-2xl font-semibold tracking-tight text-[#1f2328]">
              Authorization Gateway
            </span>
            <span className="mt-1.5 block text-base font-normal text-[#656d76]">
              ログイン
            </span>
          </h1>

          <form
            className="mt-6 w-full"
            onSubmit={(e) => {
              e.preventDefault();
            }}
          >
            <button
              type="button"
              onClick={() => {
                if (e2eLogin) {
                  router.push("/clients");
                  return;
                }
                window.location.href = `/function/${runtime}/auth/google/redirect`;
              }}
              className="inline-flex w-full items-center justify-center gap-2 rounded-md border border-[#d0d7de] bg-white px-3 py-2.5 text-sm font-medium text-[#24292f] shadow-sm transition hover:bg-gray-50 hover:border-[#b6bcc3] active:bg-gray-100"
            >
              <GoogleMark className="h-4 w-4 shrink-0" aria-hidden />
              Googleで続行
            </button>
          </form>
        </div>
      </main>

      <footer className="border-t border-[#d0d7de] bg-white py-5 text-center text-xs text-[#656d76]">
        © 2026 Authorization Gateway. All rights reserved.
      </footer>
    </div>
  );
}

function GoogleMark({ className }: { className?: string }): React.JSX.Element {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden>
      <path
        fill="#4285F4"
        d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
      />
      <path
        fill="#34A853"
        d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
      />
      <path
        fill="#FBBC05"
        d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
      />
      <path
        fill="#EA4335"
        d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
      />
    </svg>
  );
}
