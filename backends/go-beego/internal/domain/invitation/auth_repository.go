package invitation

type AuthRepository interface {
	Store(token string, ttl int) error
	Find(token string) (string, error)
	Remove(token string) error
}
