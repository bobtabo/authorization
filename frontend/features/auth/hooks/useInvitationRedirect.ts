import { useParams, useRouter } from "next/navigation";
import { useEffect } from "react";

import { apiGet } from "@/shared/api/http";
import { USER_CACHE_KEY } from "@/shared/lib/user-context";

export function useInvitationRedirect(): void {
  const { token: tokenParam } = useParams<{ token: string }>();
  const router = useRouter();

  const token = (() => {
    try { return decodeURIComponent(tokenParam ?? ""); } catch { return tokenParam ?? ""; }
  })();

  useEffect(() => {
    let cancelled = false;

    async function go() {
      if (localStorage.getItem(USER_CACHE_KEY)) {
        if (!cancelled) router.replace("/clients");
        return;
      }
      try {
        await apiGet(`/auth/invitation/${encodeURIComponent(token)}`);
        if (!cancelled) router.replace(`/login?token=${encodeURIComponent(token)}`);
      } catch {
        if (!cancelled) router.replace("/error?code=400");
      }
    }

    go();
    return () => { cancelled = true; };
  }, [token, router]);
}
