// Package adapter は永続化（DynamoDB 等）や外部 HTTP API などの境界実装を置く。
// 現状はプレースホルダー。接続クライアントは後からこのパッケージに追加する。
package adapter

// Deps はハンドラが依存する外部リソースを束ねる（DI 用）。
// フィールドは必要に応じて DynamoDB・Secrets Manager 等を追加する。
type Deps struct{}

// NewDeps は依存のデフォルト構成を返す。
func NewDeps() *Deps {
	return &Deps{}
}
