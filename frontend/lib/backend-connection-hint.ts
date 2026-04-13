import { RUNTIME_STORAGE_KEY } from "@/src/api/client";

/**
 * ログイン画面などで「どの API ベースに向いているか」を表示用テキストにまとめる。
 * 開発時は相対パス + プロキシ先、本番ビルドでは絶対 URL の想定。
 * URL 内の `/function/{runtime}/` を localStorage の選択値で置き換えて返す。
 */
export function getBackendConnectionDetail(): string {
  const rawUrl = import.meta.env.VITE_API_URL?.trim() ?? "";
  const gateway =
    import.meta.env.VITE_LAMBDA_PROXY_TARGET?.trim() ||
    import.meta.env.VITE_API_PROXY_TARGET?.trim() ||
    "";

  const runtime =
    typeof window !== "undefined"
      ? (localStorage.getItem(RUNTIME_STORAGE_KEY) ?? "php")
      : "php";

  const apiUrl = rawUrl.replace(/\/function\/[^/]+\//, `/function/${runtime}/`);

  if (apiUrl.startsWith("http://") || apiUrl.startsWith("https://")) {
    return apiUrl;
  }

  if (typeof window !== "undefined") {
    const origin = window.location.origin;
    const path = apiUrl.startsWith("/") ? apiUrl : apiUrl ? `/${apiUrl}` : "";
    let line = path ? `${origin}${path}` : origin;
    if (gateway) {
      line += `（ゲートウェイ: ${gateway.replace(/\/$/, "")}）`;
    }
    return line;
  }

  return apiUrl || "VITE_API_URL を設定してください";
}
