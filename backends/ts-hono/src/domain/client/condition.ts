/**
 * クライアントドメイン 検索条件モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** クライアント一覧の検索条件。 */
export interface ClientCondition {
  keyword?: string;
  status?: number;
}
