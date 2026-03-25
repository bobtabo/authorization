/**
 * Cloudflare Pages Functions（サイト全体のミドルウェア）
 *
 * - `functions/` はリポジトリ上で `dist/` と並列に置く（Vite は `dist` にコピーしない）。
 * - Cloudflare Pages はデプロイ時にビルド出力と別途 `functions/` を束ねる。`dist/functions/` が無いのは正常。
 *
 * Pages のプロジェクト「ルートディレクトリ」が `docs/ui-flow` であることが前提。
 */

const FRAME_ANCESTORS =
  "frame-ancestors https://www.notion.so https://*.notion.so";

type PagesMiddlewareContext = {
  next: () => Promise<Response>;
};

function applyFrameAncestorsCsp(headers: Headers): void {
  const existing = headers.get("Content-Security-Policy");

  if (!existing) {
    headers.set("Content-Security-Policy", FRAME_ANCESTORS);
    return;
  }

  if (/\bframe-ancestors\b/i.test(existing)) {
    return;
  }

  headers.set("Content-Security-Policy", `${existing.trim()}; ${FRAME_ANCESTORS}`);
}

export async function onRequest(context: PagesMiddlewareContext): Promise<Response> {
  const response = await context.next();
  const headers = new Headers(response.headers);

  applyFrameAncestorsCsp(headers);

  // X-Frame-Options は DENY / SAMEORIGIN のみが仕様。ALLOWALL は非標準で挙動が不定なため付与しない。
  // 埋め込み制御は CSP の frame-ancestors に任せる。

  return new Response(response.body, {
    status: response.status,
    statusText: response.statusText,
    headers,
  });
}
