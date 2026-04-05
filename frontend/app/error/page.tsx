"use client";

import React from "react";
import { useSearchParams } from "react-router-dom";
import { ErrorPage } from "@/components/error-page";

/**
 * 明示的に `/error` を開いたとき用（クエリで種類を切り替え可能）。
 * 例: `/error?code=500`
 */
export default function ErrorRoutePage(): React.JSX.Element {
  const [params] = useSearchParams();
  const raw = params.get("code");
  const code = raw ? Number.parseInt(raw, 10) : Number.NaN;
  const statusCode = Number.isFinite(code) && code >= 400 && code < 600 ? code : 500;

  return (
    <ErrorPage
      statusCode={statusCode}
      title={statusCode === 404 ? undefined : "サーバーで問題が発生しました"}
      message={
        statusCode === 404
          ? undefined
          : "処理を完了できませんでした。時間をおいて再度お試しください。"
      }
    />
  );
}
