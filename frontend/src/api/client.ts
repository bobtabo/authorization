import axios from "axios";

const raw = import.meta.env.VITE_API_URL?.trim();

if (!raw) {
  throw new Error(
    "VITE_API_URL is not set. Example (dev + proxy): /api/v1 — Example (prod): https://example.com/v1",
  );
}

/** 末尾スラッシュは除去（axios の結合と相性のため） */
const baseURL = raw.endsWith("/") ? raw.slice(0, -1) : raw;

/**
 * Common HTTP client.
 * Backend language is irrelevant as long as HTTP contract is matched.
 *
 * Development: `VITE_API_URL=/api/v1` のように **相対パス** にすると、
 * ブラウザは Vite と同一オリジンにだけアクセスし、vite.config の proxy がバックエンドへ転送する（CORS 回避）。
 * Production: `https://host/v1` のように絶対 URL を指定する。
 */
export const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});
