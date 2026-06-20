/**
 * クライアントユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** クライアント登録入力 DTO。 */
export interface ClientStoreInput {
  name: string;
  postCode?: string;
  pref?: string;
  city?: string;
  address?: string;
  building?: string;
  tel?: string;
  email?: string;
  executorId?: number;
}

/** クライアント更新入力 DTO。 */
export interface ClientUpdateInput {
  name?: string;
  postCode?: string;
  pref?: string;
  city?: string;
  address?: string;
  building?: string;
  tel?: string;
  email?: string;
  status?: number;
  version?: number;
}
