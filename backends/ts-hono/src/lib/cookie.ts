import type { Context } from "hono";
import { getCookie } from "hono/cookie";

export function getStaffIdFromCookie(c: Context): number {
  const raw = getCookie(c, "staff_id");
  if (!raw) return 0;
  const n = parseInt(raw, 10);
  return isNaN(n) ? 0 : n;
}

export function formatTime(d: Date | null | undefined): string | null {
  if (!d) return null;
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
