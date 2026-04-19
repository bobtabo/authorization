/**
 * axios エラーからバックエンドのエラーメッセージを取り出す。
 * バックエンドは `{ message: string }` 形式でエラーを返す。
 * 取得できない場合は fallback を返す。
 */
export function extractApiError(err: unknown, fallback: string): string {
  const data = (err as { response?: { data?: { message?: string } } })?.response?.data;
  return data?.message ?? fallback;
}
