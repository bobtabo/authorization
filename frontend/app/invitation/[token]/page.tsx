import React from "react";
import Link from "next/link";
import { formatInvitationTokenForDisplay } from "@/lib/format-invitation-url";

/** 招待リンクの着地（登録フローは API 連携後に実装） */
export default async function InvitationLandingPage({
  params,
}: {
  params: Promise<{ token: string }>;
}): Promise<React.JSX.Element> {
  const { token: tokenParam } = await params;
  let raw = tokenParam ?? "";
  try {
    raw = decodeURIComponent(raw);
  } catch {
    raw = tokenParam ?? "";
  }
  const displayToken = formatInvitationTokenForDisplay(raw);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-6 py-16">
      <div className="w-full max-w-md rounded-xl border border-gray-200 bg-white p-8 shadow-sm text-center">
        <h1 className="text-lg font-semibold text-gray-900">招待リンク</h1>
        <p className="mt-2 text-sm text-gray-600 break-all">
          トークン{" "}
          <span className="font-mono text-gray-800">{displayToken}</span>
        </p>
        <p className="mt-4 text-xs text-gray-500">
          この画面はプレースホルダーです。登録フォームは今後ここに配置します。
        </p>
        <Link
          href="/login"
          className="mt-6 inline-block text-sm font-medium text-indigo-600 hover:text-indigo-700"
        >
          ログインへ
        </Link>
      </div>
    </div>
  );
}
