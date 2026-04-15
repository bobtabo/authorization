"use client";

import { UserProvider } from "@/lib/user-context";

export function Providers({ children }: { children: React.ReactNode }): React.JSX.Element {
  return <UserProvider>{children}</UserProvider>;
}
