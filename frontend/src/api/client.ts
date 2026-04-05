import axios from "axios";

const raw = import.meta.env.VITE_API_URL?.trim();

if (!raw) {
  throw new Error(
    "VITE_API_URL is not set. Example (dev + Vite proxy): /function/api — Example (prod): https://apis.authorization-php.dev/api",
  );
}

/** 末尾スラッシュは除去（axios の結合と相性のため） */
const baseURL = raw.endsWith("/") ? raw.slice(0, -1) : raw;

/**
 * Laravel `routes/api.php` 向けの共通クライアント（ルートに /v1 は含めない）。
 *
 * - 本番例: `VITE_API_URL=https://apis.authorization-php.dev/api`
 * - 開発例: `VITE_API_URL=/function/api` → ブラウザは同一オリジンの `/function/api/...` のみ叩き、
 *   vite.config の proxy が `/function` を除いて `VITE_API_PROXY_TARGET`（例: http://localhost:8080）の `/api/...` へ転送する（CORS 回避）。
 */
export const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});
