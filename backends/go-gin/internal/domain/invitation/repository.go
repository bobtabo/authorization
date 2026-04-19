package invitation

// Repository は招待の永続化インターフェースです。
type Repository interface {
	GetCurrent() (*Vo, error)
	Issue() (*Vo, error)
	FindByToken(token string) (*Vo, error)
}
