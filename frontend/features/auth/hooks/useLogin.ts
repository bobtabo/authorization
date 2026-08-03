import { useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

import { RUNTIME_STORAGE_KEY } from "@/shared/api/client";
import { getBackendConnectionDetail } from "@/shared/lib/backend-connection-hint";
import { USER_CACHE_KEY } from "@/shared/lib/user-context";

import { RUNTIME_LABEL } from "../types";

export function useLogin() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const invitationToken = searchParams.get("token") ?? "";
  const e2eLogin = process.env.NEXT_PUBLIC_E2E === "1";
  const [runtime, setRuntime] = useState<string>("php");
  const [connectionDetail, setConnectionDetail] = useState<string>("");
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (localStorage.getItem(USER_CACHE_KEY)) {
      router.replace("/clients");
      return;
    }
    localStorage.removeItem(USER_CACHE_KEY);
    const stored = localStorage.getItem(RUNTIME_STORAGE_KEY) ?? "php";
    setRuntime(stored);
    setConnectionDetail(getBackendConnectionDetail());
    setReady(true);
  }, [router]);

  const runtimeLabel = RUNTIME_LABEL[runtime] ?? runtime;

  const startOAuth = (provider: "google" | "github") => {
    if (e2eLogin) { router.push("/clients"); return; }
    const params = invitationToken ? `?token=${encodeURIComponent(invitationToken)}` : "";
    window.location.href = `/function/${runtime}/auth/${provider}/redirect${params}`;
  };

  return { runtimeLabel, connectionDetail, ready, startOAuth };
}
