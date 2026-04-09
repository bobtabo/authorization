import React, { createContext, useContext, useEffect, useState } from "react";
import { getAuthMe } from "@/src/api/auth";

interface User {
  staff_id: number;
  name: string;
  avatar: string | null;
}

interface UserContextValue {
  user: User | null;
}

const USER_CACHE_KEY = "cachedUser";

function loadCachedUser(): User | null {
  try {
    const raw = localStorage.getItem(USER_CACHE_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  } catch {
    return null;
  }
}

const UserContext = createContext<UserContextValue>({ user: null });

export function useUser(): UserContextValue {
  return useContext(UserContext);
}

export function UserProvider({ children }: { children: React.ReactNode }): React.JSX.Element {
  // localStorage キャッシュで初期表示を即座に行い、API で最新値に更新する
  const [user, setUser] = useState<User | null>(loadCachedUser);

  useEffect(() => {
    getAuthMe()
      .then((res) => {
        const data = res as Record<string, unknown>;
        if (data.staff_id) {
          const u: User = {
            staff_id: data.staff_id as number,
            name: data.name as string,
            avatar: (data.avatar as string | null) ?? null,
          };
          localStorage.setItem(USER_CACHE_KEY, JSON.stringify(u));
          setUser(u);
        }
      })
      .catch((err: unknown) => {
        // 401 のみ未認証として扱う。5xx などの一時エラーは既存の状態を保持する
        const status = (err as { response?: { status?: number } })?.response?.status;
        if (status === 401) {
          localStorage.removeItem(USER_CACHE_KEY);
          setUser(null);
          if (window.location.pathname !== "/login") {
            window.location.href = "/login";
          }
        }
      });
  }, []);

  return <UserContext.Provider value={{ user }}>{children}</UserContext.Provider>;
}
