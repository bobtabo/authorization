package invitation

type Repository interface {
	GetCurrent() (*Vo, error)
	Issue() (*Vo, error)
	FindByToken(token string) (*Vo, error)
}
