// Package invitation は招待ドメインの型・インターフェースを提供します。
package invitation

// AuthRepository は招待認証トークンのキャッシュインターフェースです。
type AuthRepository interface {
	// Store はトークンとロールを指定秒数キャッシュします。
	Store(token string, role int, ttl int) error
	// Find はキャッシュからロールを取得します。存在しない場合は nil を返します。
	Find(token string) (*int, error)
	// Remove はトークンを削除します。
	Remove(token string) error
}
