package invitation

type AuthRepository interface {
	Store(token string, role int, ttl int) error
	Find(token string) (*int, error)
	Remove(token string) error
}
