import { MailCheck, ShieldCheck } from "lucide-react";
import Link from "next/link";
import React from "react";

/** 新規登録は招待制のため、招待リンクの利用を案内するページ。 */
export function RegisterPage(): React.JSX.Element {
  return (
    <div className="min-h-screen flex flex-col bg-[#f6f8fa]">
      <main className="flex flex-1 flex-col items-center justify-center px-4 py-12">
        <div className="flex w-full max-w-[380px] flex-col items-center">
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
              新規登録
            </span>
          </h1>

          <div className="mt-6 w-full rounded-md border border-[#d0d7de] bg-white p-5 shadow-sm">
            <p className="flex items-start gap-2 text-sm font-medium text-[#1f2328]">
              <MailCheck className="mt-0.5 h-4 w-4 shrink-0 text-indigo-600" aria-hidden />
              新規登録は招待制です
            </p>
            <p className="mt-2 text-sm leading-relaxed text-[#656d76]">
              アカウントは管理者からの招待でのみ作成できます。届いた招待リンクを開き、Google
              または GitHub アカウントで登録してください。
            </p>
            <p className="mt-2 text-sm leading-relaxed text-[#656d76]">
              招待リンクをお持ちでない場合は、管理者に招待を依頼してください。
            </p>
          </div>

          <p className="mt-6 text-center text-sm text-[#656d76]">
            すでにアカウントをお持ちの方は{" "}
            <Link
              href="/login"
              className="font-medium text-indigo-600 hover:text-indigo-700"
            >
              ログイン
            </Link>
          </p>
        </div>
      </main>

      <footer className="border-t border-[#d0d7de] bg-white py-5 text-center text-xs text-[#656d76]">
        © 2026 Authorization Gateway. All rights reserved.
      </footer>
    </div>
  );
}
