import React from "react";
import { getInitials } from "@/lib/session-user";

/**
 * ヘッダー・スタッフ一覧で同じ見た目のアバターを使う
 */
export function UserAvatar({
  name,
  avatarUrl,
  className = "",
}: {
  name: string;
  avatarUrl?: string | null;
  className?: string;
}): React.JSX.Element {
  if (avatarUrl) {
    return (
      <img
        src={avatarUrl}
        alt={name}
        className={`h-7 w-7 shrink-0 rounded-full object-cover shadow-sm ring-2 ring-white ${className}`}
        aria-hidden
      />
    );
  }

  return (
    <span
      className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-violet-600 text-[10px] font-semibold leading-none text-white shadow-sm ring-2 ring-white ${className}`}
      aria-hidden
    >
      {getInitials(name)}
    </span>
  );
}
