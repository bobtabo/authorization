/**
 * Cookie / 日時フォーマット ユーティリティモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import type { Context } from "hono";
import { getCookie } from "hono/cookie";

/**
 * Cookie からスタッフ ID を取得します。
 * @param c - Hono コンテキスト
 * @returns スタッフ ID（未設定または不正値の場合は 0）
 */
export function getStaffIdFromCookie(c: Context): number {
  const raw = getCookie(c, "staff_id");
  if (!raw) return 0;
  const n = parseInt(raw, 10);
  return isNaN(n) ? 0 : n;
}

/**
 * Date を "YYYY-MM-DD HH:mm" 形式にフォーマットします。
 * @param d - 対象日時
 * @returns フォーマット済み文字列、または null
 */
export function formatTime(d: Date | null | undefined): string | null {
  if (!d) return null;
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
