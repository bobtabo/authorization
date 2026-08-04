import { useEffect, useState } from "react";

import { getClientQr, type ClientQr } from "../api";

export function useClientQr(params: Promise<{ identifier: string }>) {
  const [data, setData] = useState<ClientQr | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    const failureMessage = "QRコードの取得に失敗しました。担当者にお問い合わせください。";

    params
      .then(({ identifier }) => getClientQr(identifier))
      .then((result) => {
        if (!ignore) setData(result);
      })
      .catch(() => {
        if (!ignore) setError(failureMessage);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [params]);

  return { data, error, loading };
}
