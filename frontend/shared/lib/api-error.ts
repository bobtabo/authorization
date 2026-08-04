/**
 * axios エラーからバックエンドのエラーメッセージを取り出す。
 * バックエンドは `{ message: string }` 形式でエラーを返す。
 * 取得できない場合は fallback を返す。
 */
export function extractApiError(err: unknown, fallback: string): string {
  const data = (err as { response?: { data?: { message?: unknown } } })?.response?.data;
  const message = data?.message;
  return typeof message === "string" && message.trim() !== "" ? message : fallback;
}
