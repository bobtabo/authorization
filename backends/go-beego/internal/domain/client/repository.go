// Package client はクライアントドメインのインターフェースを定義します。
package client

import "time"

// JwtHistoryRepository は JWT 履歴の永続化インターフェースです。
type JwtHistoryRepository interface {
	FindByCondition(cond JwtHistoryCondition) ([]*JwtHistory, error)
	CountByCondition(cond JwtHistoryCondition) (int, error)
	Save(clientID uint64, memberID string, issueAt time.Time, jwt string) error
}

// Repository はクライアントの永続化インターフェースです。
type Repository interface {
	// FindByCondition は条件に合うクライアント一覧を取得します。
	FindByCondition(cond Condition) ([]*Client, error)
	// CountByCondition は検索条件に合致するクライアントの総件数を返します。
	CountByCondition(cond Condition) (int, error)
	// FindByID はIDでクライアントを取得します。
	FindByID(c *Client) (*Client, error)
	// FindByAccessToken はアクセストークンでクライアントを取得します。
	FindByAccessToken(c *Client) (*Client, error)
	// FindByIdentifier はidentifierでクライアントを取得します。
	FindByIdentifier(c *Client) (*Client, error)
	// Save はクライアントを登録または更新します。
	Save(c *Client) (*Client, error)
	// SoftDelete はクライアントを論理削除します。
	SoftDelete(c *Client) error
}
