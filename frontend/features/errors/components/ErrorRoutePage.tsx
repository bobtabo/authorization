"use client";

import React, { Suspense } from "react";

import { ErrorPage } from "@/shared/components/error-page";

import { useErrorStatusCode } from "../hooks/useErrorStatusCode";

function ErrorRoutePageContent(): React.JSX.Element {
  const statusCode = useErrorStatusCode();

  return <ErrorPage statusCode={statusCode} />;
}

/**
 * 明示的に `/error` を開いたとき用（クエリで種類を切り替え可能）。
 * 例: `/error?code=500`
 */
export function ErrorRoutePage(): React.JSX.Element {
  return (
    <Suspense>
      <ErrorRoutePageContent />
    </Suspense>
  );
}
