package client

import "time"

// JwtHistoryRepository は JWT 履歴の永続化インターフェースです。
type JwtHistoryRepository interface {
	FindByClientID(clientID uint64) ([]*JwtHistory, error)
	Save(clientID uint64, memberID string, issueAt time.Time, jwt string) error
}

// Repository はクライアントの永続化インターフェースです。
type Repository interface {
	// FindByCondition は検索条件に合致するクライアントエンティティを返します。
	FindByCondition(cond Condition) ([]*Client, error)
	// FindByID はIDでクライアントエンティティを返します。存在しない場合は nil を返します。
	FindByID(id uint64) (*Client, error)
	// FindByAccessToken はアクセストークンでアクティブなクライアントエンティティを返します。
	FindByAccessToken(token string) (*Client, error)
	// FindByIdentifier は識別子でクライアントエンティティを返します。
	FindByIdentifier(identifier string) (*Client, error)
	// Save はクライアントエンティティを保存（新規作成または更新）して返します。
	Save(c *Client) (*Client, error)
	// SoftDelete はクライアントを論理削除します。version が DB と一致しない場合は楽観排他エラーを返します。
	SoftDelete(id uint64, deletedBy uint, version int) error
}
