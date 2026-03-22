const INVITATION_SEGMENT = "/invitation/";

/**
 * 招待URLのうち `/invitation/` 以降のトークンが長い場合、先頭・末尾のみ表示して `...` で省略する。
 * コピー用の完全URLは別途保持すること。
 */
export function formatInvitationUrlForDisplay(
  fullUrl: string,
  tokenHead = 6,
  tokenTail = 4
): string {
  if (!fullUrl) return "";
  const idx = fullUrl.indexOf(INVITATION_SEGMENT);
  if (idx === -1) {
    return fullUrl.length > 72 ? `${fullUrl.slice(0, 68)}...` : fullUrl;
  }

  const base = fullUrl.slice(0, idx + INVITATION_SEGMENT.length);
  let after = fullUrl.slice(idx + INVITATION_SEGMENT.length);
  const qOrHash = after.search(/[?#]/);
  const suffix = qOrHash === -1 ? "" : after.slice(qOrHash);
  const token = qOrHash === -1 ? after : after.slice(0, qOrHash);

  if (token.length <= tokenHead + tokenTail + 3) {
    return fullUrl;
  }

  return `${base}${token.slice(0, tokenHead)}...${token.slice(-tokenTail)}${suffix}`;
}

/** トークン文字列だけ省略表示するとき */
export function formatInvitationTokenForDisplay(
  token: string,
  head = 6,
  tail = 4
): string {
  if (!token) return "";
  if (token.length <= head + tail + 3) return token;
  return `${token.slice(0, head)}...${token.slice(-tail)}`;
}
