import React from "react";
import { getInitials } from "@/lib/session-user";

/**
 * ヘッダー・アカウント一覧で同じ見た目のアバターを使う
 */
export function UserAvatar({
  name,
  className = "",
}: {
  name: string;
  className?: string;
}): React.JSX.Element {
  return (
    <span
      className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-violet-600 text-[10px] font-semibold leading-none text-white shadow-sm ring-2 ring-white ${className}`}
      aria-hidden
    >
      {getInitials(name)}
    </span>
  );
}
