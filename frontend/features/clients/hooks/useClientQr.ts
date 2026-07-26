import { useEffect, useState } from "react";

import { getClientQr, type ClientQr } from "../api";

export function useClientQr(params: Promise<{ identifier: string }>) {
  const [data, setData] = useState<ClientQr | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    params.then(({ identifier }) => {
      getClientQr(identifier)
        .then(setData)
        .catch(() => setError("QRコードの取得に失敗しました。担当者にお問い合わせください。"))
        .finally(() => setLoading(false));
    });
  }, [params]);

  return { data, error, loading };
}
