package invitation

type Repository interface {
	GetCurrentByRole(role int) (*Vo, error)
	IssueByRole(role int) (*Vo, error)
	FindByToken(token string) (*Vo, error)
}
