/**
 * ログイン画面などで「どの API ベースに向いているか」を表示用テキストにまとめる。
 * 開発時は相対パス + プロキシ先、本番ビルドでは絶対 URL の想定。
 */
export function getBackendConnectionDetail(): string {
  const apiUrl = import.meta.env.VITE_API_URL?.trim() ?? "";
  const gateway =
    import.meta.env.VITE_LAMBDA_PROXY_TARGET?.trim() ||
    import.meta.env.VITE_API_PROXY_TARGET?.trim() ||
    "";

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
