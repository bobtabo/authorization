/**
 * スタッフユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** スタッフロール更新入力 DTO。 */
export interface StaffUpdateRoleInput {
  staffId: number;
  role: number;
  executorId: number;
}

/** スタッフ論理削除入力 DTO。 */
export interface StaffDestroyInput {
  staffId: number;
  executorId: number;
}
