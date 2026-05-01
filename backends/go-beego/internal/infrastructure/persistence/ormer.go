package persistence

import "github.com/beego/beego/v2/client/orm"

// QueryOrmer は orm.Ormer と orm.TxOrmer が共有するクエリメソッドを抽象化します。
// リポジトリに渡す型として使い、通常操作とトランザクション操作の両方に対応します。
type QueryOrmer interface {
	Insert(md interface{}) (int64, error)
	Update(md interface{}, cols ...string) (int64, error)
	QueryTable(ptrStructOrTableName interface{}) orm.QuerySeter
	Raw(query string, args ...interface{}) orm.RawSeter
}
