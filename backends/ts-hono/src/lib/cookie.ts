/**
 * Cookie / 日時フォーマット ユーティリティモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import type { Context } from "hono";
import { getCookie } from "hono/cookie";
import { config } from "../config.js";
import { verifyStaffId } from "./staffSession.js";

/**
 * Cookie から署名済みスタッフ ID を取得します。
 * @param c - Hono コンテキスト
 * @returns 検証済みのスタッフ ID（未設定・不正値・期限切れの場合は 0）
 */
export function getStaffIdFromCookie(c: Context): number {
  const raw = getCookie(c, "staff_id");
  return verifyStaffId(raw, config.app.staffCookieSecret);
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
