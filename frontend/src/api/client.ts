import axios from "axios";

const raw = process.env.NEXT_PUBLIC_API_URL?.trim();

if (!raw) {
  throw new Error(
    "NEXT_PUBLIC_API_URL is not set. Example (dev + Next.js proxy): /function/php/api — Example (prod): https://apis.authorization-php.dev/api",
  );
}

/** バックエンドランタイム選択を永続化する localStorage キー */
export const RUNTIME_STORAGE_KEY = "backend-runtime";

/**
 * URL 内の `/function/{runtime}/` セグメントを localStorage の選択値で置き換えます。
 * URL がこのパターンを含まない場合（本番 URL 等）はそのまま返します。
 */
function resolveBaseUrl(envUrl: string): string {
  const runtime =
    (typeof window !== "undefined" ? localStorage.getItem(RUNTIME_STORAGE_KEY) : null) ?? "php";
  const resolved = envUrl.replace(/\/function\/[^/]+\//, `/function/${runtime}/`);
  return resolved.endsWith("/") ? resolved.slice(0, -1) : resolved;
}

/**
 * バックエンド向けの共通クライアント。
 * baseURL はページロード時に localStorage のランタイム選択を反映して決定します。
 *
 * - 開発例: `NEXT_PUBLIC_API_URL=/function/php/api` → `/function/{runtime}/api`（Next.js rewrites 経由）
 * - 本番例: `NEXT_PUBLIC_API_URL=https://apis.authorization-php.dev/api`
 */
export const apiClient = axios.create({
  baseURL: resolveBaseUrl(raw),
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
  timeout: 10000,
});
