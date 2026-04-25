/**
 * 認証ユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** ログイン入力 DTO。 */
export interface LoginInput {
  provider: number;
  providerId: string;
  name: string;
  email: string;
  avatar?: string;
}
