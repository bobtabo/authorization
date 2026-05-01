/**
 * Gate ドメイン バリューオブジェクトモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** Gate JWT 発行結果 VO。 */
export interface GateIssueVo {
  token: string;
}

/** Gate JWT 検証結果 VO。 */
export interface GateVerifyVo {
  identifier: string;
  member: string | undefined;
  fingerprint: string | null;
  payload: Record<string, unknown>;
}
