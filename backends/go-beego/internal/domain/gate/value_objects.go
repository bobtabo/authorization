package gate

type IssueVo struct {
	Token string
}

type VerifyVo struct {
	Claims map[string]interface{}
}

type CacheRepository interface {
	GetJwt(identifier, memberID string) (string, error)
	PutJwt(identifier, memberID, token string, ttl int) error
}
