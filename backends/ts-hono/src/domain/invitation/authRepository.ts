/**
 * 招待認証リポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** 招待認証トークンのキャッシュリポジトリインターフェース。 */
export interface InvitationAuthRepository {
  /** トークンを指定秒数キャッシュします。 */
  store(token: string, ttl: number): Promise<void>;
  /** トークンを取得します。存在しない場合は null を返します。 */
  find(token: string): Promise<string | null>;
  /** トークンを削除します。 */
  remove(token: string): Promise<void>;
}
