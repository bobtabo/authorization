/**
 * ゲートユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** JWT 発行入力 DTO。 */
export interface GateIssueInput {
  accessToken: string;
  member: string;
}

/** JWT 検証入力 DTO。 */
export interface GateVerifyInput {
  identifier: string;
  token: string;
}
