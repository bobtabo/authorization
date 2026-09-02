import { useSearchParams } from "next/navigation";

/** `?code=` クエリから表示するHTTPステータスコードを決定する。4xx/5xxの3桁以外はデフォルト(500)にフォールバックする。 */
export function useErrorStatusCode(): number {
  const params = useSearchParams();
  const raw = params.get("code");
  return raw && /^[45]\d{2}$/.test(raw) ? Number.parseInt(raw, 10) : 500;
}
