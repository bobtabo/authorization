/**
 * クライアントドメイン バリューオブジェクトモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** クライアント一覧レスポンス用 VO。 */
export interface ClientListItem {
  id: number;
  name: string;
  identifier: string;
  status: number;
  startedAt: Date | null;
  stoppedAt: Date | null;
  createdAt: Date | null;
  updatedAt: Date | null;
}

/** クライアント詳細レスポンス用 VO。 */
export interface ClientDetailVo {
  id: number;
  name: string;
  identifier: string;
  postCode: string;
  pref: string;
  city: string;
  address: string;
  building: string;
  tel: string;
  email: string;
  status: number;
  fingerprint: string | null;
  startedAt: Date | null;
  stoppedAt: Date | null;
  createdAt: Date | null;
  updatedAt: Date | null;
}

/** クライアント登録結果 VO。メール送信・通知配信に必要なフィールドを含む。 */
export interface ClientStoreResultVo {
  id: number;
  name: string;
  identifier: string;
  email: string;
  token: string;
}

/** クライアント登録入力 VO（リポジトリ INSERT 用）。 */
export interface ClientStoreVo {
  name: string;
  identifier: string;
  postCode?: string;
  pref?: string;
  city?: string;
  address?: string;
  building?: string;
  tel?: string;
  email?: string;
}

/** クライアント更新入力 VO（リポジトリ UPDATE 用）。 */
export interface ClientUpdateVo {
  name?: string;
  postCode?: string;
  pref?: string;
  city?: string;
  address?: string;
  building?: string;
  tel?: string;
  email?: string;
  status?: number;
}

/** QRコード返却用 VO。 */
export interface ClientQrVo {
  identifier: string;
  deeplinkUrl: string;
}

/** スマホアプリ向けクライアント情報 VO。 */
export interface ClientInfoVo {
  identifier: string;
  name: string;
  status: number;
}

/** 利用開始結果 VO。 */
export interface ClientStartVo {
  accessToken: string;
}
