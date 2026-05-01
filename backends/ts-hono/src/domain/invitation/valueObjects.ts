/**
 * 招待ドメイン 値オブジェクトモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** 招待トークン取得・発行結果の VO。 */
export interface InvitationResult {
  token: string;
  url: string;
  displayUrl: string;
}
