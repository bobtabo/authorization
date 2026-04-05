/**
 * API から返ってくるタイムスタンプ文字列を "yyyy-mm-dd hh:mm" 形式に整形します。
 * null / undefined / 空文字は fallback（デフォルト "—"）を返します。
 */
export function formatTimestamp(value: string | null | undefined, fallback = "—"): string {
  if (!value) return fallback;
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return fallback;
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}
