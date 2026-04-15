import { useEffect, useState } from "react";
import { fetchPostcodeJp, type PostcodeJpRow } from "@/lib/postcode-jp";

const DEBOUNCE_MS = 400;

/**
 * 郵便番号が 7 桁になったら Postcode JP で検索。結果は件数分すべて返す（複数市区町村に対応）。
 */
export function usePostcodeJpLookup(postalCode: string): {
  loading: boolean;
  error: string | null;
  rows: PostcodeJpRow[];
} {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [rows, setRows] = useState<PostcodeJpRow[]>([]);

  useEffect(() => {
    const digits = postalCode.replace(/\D/g, "");
    const apiKey = process.env.NEXT_PUBLIC_POSTCODE_API_KEY ?? "";

    if (digits.length !== 7) {
      setLoading(false);
      setError(null);
      setRows([]);
      return;
    }

    // 別の 7 桁に切り替えた直後は古い候補を出さない
    setRows([]);
    setError(null);

    const timer = setTimeout(() => {
      void (async () => {
        if (!apiKey) {
          setError("POSTCODE_API_KEY が未設定です（.env を確認）");
          setRows([]);
          setLoading(false);
          return;
        }

        setLoading(true);
        setError(null);
        try {
          const data = await fetchPostcodeJp(digits, apiKey);
          if (data.length === 0) {
            setError("該当する住所が見つかりませんでした");
            setRows([]);
            return;
          }
          setRows(data);
        } catch {
          setError("住所の取得に失敗しました");
          setRows([]);
        } finally {
          setLoading(false);
        }
      })();
    }, DEBOUNCE_MS);

    return () => clearTimeout(timer);
  }, [postalCode]);

  return { loading, error, rows };
}
