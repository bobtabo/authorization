package persistence

import "strings"

// escapeLikeKeywordForContains は beego ORM の `__contains` フィルタ用にキーワードを
// エスケープします。
//
// beego ORM は `__contains` の値に含まれる `%` を自前で `\%` にエスケープするため
// （client/orm.db.go の getCondSQL 参照）、ここで重ねてエスケープすると二重エスケープに
// なる。`\` と `_` は beego 側でエスケープされないため、ここで先にエスケープする
// （`\` は他の文字のエスケープに使うため最初に変換する）。
func escapeLikeKeywordForContains(keyword string) string {
	replacer := strings.NewReplacer(`\`, `\\`, `_`, `\_`)
	return replacer.Replace(keyword)
}
