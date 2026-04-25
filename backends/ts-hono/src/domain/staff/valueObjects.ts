/**
 * スタッフドメイン バリューオブジェクトモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** スタッフ一覧レスポンス用 VO。 */
export interface StaffListItem {
  id: number;
  name: string;
  email: string;
  role: number;
  /** 0=削除済み, 1=有効 */
  status: number;
  createdAt: Date | null;
  updatedAt: Date | null;
}

/** ログイン中スタッフ情報レスポンス用 VO。 */
export interface StaffVo {
  id: number;
  name: string;
  avatar: string | null;
  role: number;
}

/** スタッフ upsert 入力 VO（リポジトリ用）。 */
export interface StaffUpsertVo {
  provider: number;
  providerId: string;
  name: string;
  email: string;
  avatar?: string;
  role?: number;
}
