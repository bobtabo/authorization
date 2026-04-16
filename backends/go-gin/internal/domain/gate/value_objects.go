package gate

// IssueVo は JWT 発行レスポンス用の値オブジェクトです。
type IssueVo struct {
	Token string
}

// VerifyVo は JWT 検証レスポンス用の値オブジェクトです。
type VerifyVo struct {
	Claims map[string]interface{}
}

// CacheRepository は Gate JWT のキャッシュインターフェースです。
type CacheRepository interface {
	GetJwt(identifier, memberID string) (string, error)
	PutJwt(identifier, memberID, token string, ttl int) error
}
