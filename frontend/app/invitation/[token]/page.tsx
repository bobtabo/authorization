"use client";

import React, { useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { apiGet } from "@/src/api/http";
import { RUNTIME_STORAGE_KEY } from "@/src/api/client";

export default function InvitationLandingPage(): React.JSX.Element {
  const { token: tokenParam } = useParams<{ token: string }>();
  const router = useRouter();

  const token = (() => {
    try {
      return decodeURIComponent(tokenParam ?? "");
    } catch {
      return tokenParam ?? "";
    }
  })();

  useEffect(() => {
    let cancelled = false;

    async function go() {
      try {
        await apiGet(`/auth/invitation/${token}`);
        if (cancelled) return;
        const runtime = localStorage.getItem(RUNTIME_STORAGE_KEY) ?? "php";
        window.location.href = `/function/${runtime}/auth/google/redirect?token=${token}`;
      } catch {
        if (cancelled) return;
        router.replace("/error?code=400");
      }
    }

    go();
    return () => {
      cancelled = true;
    };
  }, [token, router]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <p className="text-sm text-gray-500">確認中...</p>
    </div>
  );
}
