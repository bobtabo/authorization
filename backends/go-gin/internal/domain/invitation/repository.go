package invitation

// Repository は招待の永続化インターフェースです。
type Repository interface {
	// GetCurrentByRole はロールで絞り込んだ最新の招待情報の値オブジェクトを返します。
	GetCurrentByRole(role int) (*Vo, error)
	// Issue は新しい招待トークンを生成して保存し、値オブジェクトを返します。
	Issue(role int) (*Vo, error)
	// FindByToken はトークンで招待情報の値オブジェクトを返します。
	FindByToken(token string) (*Vo, error)
}
