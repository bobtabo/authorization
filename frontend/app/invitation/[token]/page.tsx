"use client";

import React, { useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { apiGet } from "@/src/api/http";
import { USER_CACHE_KEY } from "@/lib/user-context";

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
      if (localStorage.getItem(USER_CACHE_KEY)) {
        router.replace("/clients");
        return;
      }
      try {
        await apiGet(`/auth/invitation/${token}`);
        if (cancelled) return;
        router.replace(`/login?token=${encodeURIComponent(token)}`);
      } catch {
        if (cancelled) return;
        router.replace("/error?code=400");
      }
    }

    go();
    return () => { cancelled = true; };
  }, [token, router]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <p className="text-sm text-gray-500">確認中...</p>
    </div>
  );
}
