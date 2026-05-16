package invitation

type Repository interface {
	GetCurrentByRole(role int) (*Vo, error)
	Issue(role int) (*Vo, error)
	FindByToken(token string) (*Vo, error)
}
