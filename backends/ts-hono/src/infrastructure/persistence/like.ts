/**
 * LIKE検索用のエスケープ処理モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/**
 * LIKE検索用にキーワードをエスケープします。
 *
 * キーワードに含まれる `\`/`%`/`_` はLIKEパターン上でエスケープ文字・ワイルドカードとして
 * 解釈されるため、リテラル文字列として一致させるには事前にエスケープする必要がある。
 * `\` は他の文字のエスケープに使うため最初に変換する。
 *
 * @param keyword - エスケープ対象のキーワード
 * @returns エスケープ済みのキーワード
 */
export function escapeLikeKeyword(keyword: string): string {
  return keyword.replace(/\\/g, "\\\\").replace(/%/g, "\\%").replace(/_/g, "\\_");
}
