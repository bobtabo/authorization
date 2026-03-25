import React, { useCallback, useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { ClipboardCopy, X, Loader2 } from "lucide-react";
import { apiClient } from "@/src/api/client";
import { formatInvitationUrlForDisplay } from "@/lib/format-invitation-url";

/** API 未実装・500・CORS 等のときに見せる、それっぽいモックURL */
function buildMockInvitationUrl(): string {
  const bytes = new Uint8Array(24);
  crypto.getRandomValues(bytes);
  const token = Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
  const origin = typeof window !== "undefined" ? window.location.origin : "";
  return `${origin}/invitation/${token}`;
}

type Props = {
  open: boolean;
  onClose: () => void;
};

export function InvitationUrlModal({ open, onClose }: Props): React.JSX.Element {
  const [url, setUrl] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);
  const [reissuing, setReissuing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState<boolean>(false);
  const [isMock, setIsMock] = useState<boolean>(false);

  const loadUrl = useCallback(async () => {
    setError(null);
    setLoading(true);
    try {
      const { data } = await apiClient.get<{ url: string }>("/invitation");
      setUrl(typeof data.url === "string" ? data.url : buildMockInvitationUrl());
      setIsMock(false);
    } catch {
      setUrl(buildMockInvitationUrl());
      setIsMock(true);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!open) return;
    void loadUrl();
  }, [open, loadUrl]);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape" && !reissuing) onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose, reissuing]);

  const handleCopy = async () => {
    if (!url) return;
    try {
      await navigator.clipboard.writeText(url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      setError("クリップボードにコピーできませんでした");
    }
  };

  const displayUrl = url ? formatInvitationUrlForDisplay(url) : "";

  const handleReissue = async () => {
    setError(null);
    setReissuing(true);
    try {
      const { data } = await apiClient.get<{ url: string }>("/invitation/issue");
      setUrl(typeof data.url === "string" ? data.url : buildMockInvitationUrl());
      setIsMock(false);
    } catch {
      setUrl(buildMockInvitationUrl());
      setIsMock(true);
    } finally {
      setReissuing(false);
    }
  };

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 p-4"
          onClick={() => !reissuing && onClose()}
          role="presentation"
        >
          <motion.div
            initial={{ scale: 0.96, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.96, opacity: 0 }}
            onClick={(e) => e.stopPropagation()}
            className="w-full max-w-lg rounded-xl border border-gray-200 bg-white shadow-xl"
            role="dialog"
            aria-modal="true"
            aria-labelledby="invitation-modal-title"
          >
            <div className="flex items-center justify-between border-b border-gray-100 px-5 py-4">
              <h2 id="invitation-modal-title" className="text-lg font-semibold text-gray-900">
                招待URL
              </h2>
              <button
                type="button"
                onClick={() => !reissuing && onClose()}
                className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
                aria-label="閉じる"
              >
                <X size={20} />
              </button>
            </div>

            <div className="space-y-4 px-5 py-5">
              {isMock && (
                <div className="rounded-lg border border-indigo-200 bg-indigo-50 px-3 py-2 text-sm text-indigo-900">
                  APIが未接続・エラー時のため、モックの招待URLを表示しています（見た目の確認用です）。
                </div>
              )}
              {error && (
                <div className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-800">
                  {error}
                </div>
              )}

              <div className="space-y-2">
                <label htmlFor="invitation-url-field" className="block text-sm font-medium text-gray-700">
                  現在の招待URL
                </label>
                <div className="relative">
                  <input
                    id="invitation-url-field"
                    readOnly
                    value={loading ? "読み込み中…" : displayUrl}
                    className="w-full rounded-lg border border-gray-300 bg-gray-50 px-3 py-3 font-mono text-sm text-gray-800"
                    title={url}
                    aria-label={url ? `完全なURL（コピーは全文）: ${url}` : undefined}
                  />
                  {loading && (
                    <div className="pointer-events-none absolute inset-0 flex items-center justify-center rounded-lg bg-white/60">
                      <Loader2 className="h-5 w-5 animate-spin text-indigo-600" aria-hidden />
                    </div>
                  )}
                </div>
                <p className="text-xs text-gray-500">
                  表示は `/invitation/` 以降のトークンが長い場合に「...」で省略しています。コピーでは常に全文が入ります。
                </p>
              </div>

              <button
                type="button"
                onClick={handleCopy}
                disabled={!url || loading || reissuing}
                className="flex w-full items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-3.5 text-base font-semibold text-white shadow-sm transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ClipboardCopy size={20} aria-hidden />
                {copied ? "コピーしました！" : "クリップボードにコピー"}
              </button>

              <div className="border-t border-gray-100 pt-4">
                <button
                  type="button"
                  onClick={handleReissue}
                  disabled={loading || reissuing}
                  className="flex w-full items-center justify-center gap-2 rounded-lg border-2 border-rose-600 bg-rose-50 px-4 py-3 text-sm font-semibold text-rose-800 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {reissuing ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" aria-hidden />
                      再発行中…
                    </>
                  ) : (
                    "URLを再発行"
                  )}
                </button>
                <p className="mt-2 text-xs text-gray-500">
                  再発行すると以前の招待URLは無効になります。サーバー上のトークンを更新し、この画面の表示だけが即座に切り替わります。
                </p>
              </div>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
