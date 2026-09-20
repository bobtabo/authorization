package persistence

import "strings"

// escapeLikeKeyword はLIKE検索用にキーワードをエスケープします。
//
// キーワードに含まれる `\`/`%`/`_` はLIKEパターン上でエスケープ文字・ワイルドカードとして
// 解釈されるため、リテラル文字列として一致させるには事前にエスケープする必要がある。
// `\` は他の文字のエスケープに使うため最初に変換する。
func escapeLikeKeyword(keyword string) string {
	replacer := strings.NewReplacer(`\`, `\\`, `%`, `\%`, `_`, `\_`)
	return replacer.Replace(keyword)
}
