package invitation

// Repository は招待の永続化インターフェースです。
type Repository interface {
	// GetCurrent は最新の招待情報の値オブジェクトを返します。
	GetCurrent() (*Vo, error)
	// Issue は新しい招待トークンを生成して保存し、値オブジェクトを返します。
	Issue() (*Vo, error)
	// FindByToken はトークンで招待情報の値オブジェクトを返します。
	FindByToken(token string) (*Vo, error)
}
