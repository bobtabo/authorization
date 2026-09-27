/*
 * LIKE検索用のエスケープ処理モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

/**
 * LIKE検索用にキーワードをエスケープします。
 *
 * キーワードに含まれる `\`/`%`/`_` はLIKEパターン上でエスケープ文字・ワイルドカードとして
 * 解釈されるため、リテラル文字列として一致させるには事前にエスケープする必要がある。
 * `\` は他の文字のエスケープに使うため最初に変換する。
 *
 * @param keyword エスケープ対象のキーワード
 * @return エスケープ済みのキーワード
 */
fun escapeLikeKeyword(keyword: String): String =
    keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
