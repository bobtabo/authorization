/**
 * 招待認証リポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** 招待認証トークンのキャッシュリポジトリインターフェース。 */
export interface InvitationAuthRepository {
  /** トークンとロールを指定秒数キャッシュします。 */
  store(token: string, role: number, ttl: number): Promise<void>;
  /** トークンに紐づくロールを取得します。存在しない場合は null を返します。 */
  find(token: string): Promise<number | null>;
  /** トークンを削除します。 */
  remove(token: string): Promise<void>;
}
