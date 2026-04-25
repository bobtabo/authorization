/**
 * 招待ユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** 招待トークン取得出力 DTO。 */
export interface InvitationCurrentOutput {
  token: string;
  url: string;
  displayUrl: string;
}

/** 招待トークン発行出力 DTO。 */
export interface InvitationIssueOutput {
  token: string;
  url: string;
  displayUrl: string;
}
