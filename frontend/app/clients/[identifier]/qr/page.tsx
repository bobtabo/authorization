"use client";

import { ShieldCheck, Loader2, AlertCircle } from "lucide-react";
import { QRCodeSVG } from "qrcode.react";
import React, { useEffect, useState } from "react";

import { getClientQr, type ClientQr } from "@/src/api/clients";

type Props = {
  params: Promise<{ identifier: string }>;
};

export default function ClientQrPage({ params }: Props): React.JSX.Element {
  const [data, setData] = useState<ClientQr | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    params.then(({ identifier }) => {
      getClientQr(identifier)
        .then(setData)
        .catch(() => setError("QRコードの取得に失敗しました。担当者にお問い合わせください。"))
        .finally(() => setLoading(false));
    });
  }, [params]);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-6 h-14 flex items-center">
          <div className="flex items-center gap-3">
            <div className="w-9 h-8 rounded-lg bg-indigo-600 text-white flex items-center justify-center">
              <ShieldCheck size={16} />
            </div>
            <span className="text-sm font-semibold text-gray-800">Authorization Gateway</span>
          </div>
        </div>
      </header>

      <main className="flex-1 flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-md">
          <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
            <div className="bg-indigo-50 border-b border-indigo-100 px-8 py-6">
              <h1 className="text-xl font-semibold text-gray-900">スマホアプリ連携</h1>
              <p className="text-sm text-gray-600 mt-1">
                スマホアプリでQRコードを読み取ってください
              </p>
            </div>

            <div className="px-8 py-8 flex flex-col items-center gap-6">
              {loading && (
                <div className="flex items-center gap-2 text-gray-500">
                  <Loader2 size={20} className="animate-spin" />
                  <span className="text-sm">読み込み中...</span>
                </div>
              )}

              {error && (
                <div className="w-full flex items-start gap-3 bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-lg">
                  <AlertCircle size={18} className="shrink-0 mt-0.5" />
                  <span>{error}</span>
                </div>
              )}

              {data && (
                <>
                  <div className="p-4 bg-white border border-gray-200 rounded-xl shadow-sm">
                    <QRCodeSVG
                      value={data.deeplink_url}
                      size={220}
                      level="M"
                      includeMargin={false}
                    />
                  </div>
                  <p className="text-xs text-gray-400 text-center">
                    スマホアプリをインストール後、このQRコードを読み取ってください。
                  </p>
                </>
              )}
            </div>
          </div>
        </div>
      </main>

      <footer className="border-t border-gray-200 bg-white py-6 text-center text-xs text-gray-400">
        © {new Date().getFullYear()} Authorization Gateway. All rights reserved.
      </footer>
    </div>
  );
}
