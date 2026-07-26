/**
 * モック: ログイン中ユーザー（ヘッダー表示とスタッフ一覧のいずれかと一致させる）
 */
export const SESSION_USER_DISPLAY_NAME = "山田 太郎";

export function getInitials(name: string): string {
  const t = name.trim();
  if (!t) return "?";
  const parts = t.split(/\s+/);
  if (parts.length >= 2 && parts[0] && parts[1]) {
    return `${parts[0].slice(0, 1)}${parts[1].slice(0, 1)}`;
  }
  return t.slice(0, 2);
}
