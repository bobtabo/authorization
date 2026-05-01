/**
 * スタッフドメイン 検索条件モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** スタッフ一覧の検索条件。 */
export interface StaffCondition {
  keyword?: string;
  roles?: number[];
}
