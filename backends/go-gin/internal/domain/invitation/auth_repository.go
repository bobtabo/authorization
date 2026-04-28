// Package invitation は招待ドメインの型・インターフェースを提供します。
package invitation

// AuthRepository は招待認証トークンのキャッシュインターフェースです。
type AuthRepository interface {
	// Store はトークンを指定秒数キャッシュします。
	Store(token string, ttl int) error
	// Find はトークンを取得します。存在しない場合は空文字を返します。
	Find(token string) (string, error)
	// Remove はトークンを削除します。
	Remove(token string) error
}
