package client

import "time"

// JwtHistory は JWT 履歴のドメインエンティティです。
type JwtHistory struct {
	ID       uint64
	ClientID uint64
	MemberID string
	IssueAt  time.Time
	Jwt      string
}

// JwtHistoryCondition は JWT 履歴の検索条件です。
type JwtHistoryCondition struct {
	ClientID uint64
	Offset   int
	Limit    int
	Sort     string
	SortType string
}

// Client はクライアントのドメインエンティティです（GORM タグなし）。
type Client struct {
	ID          uint64
	Name        string
	Identifier  string
	PostCode    string
	Pref        string
	City        string
	Address     string
	Building    string
	Tel         string
	Email       string
	AccessToken string
	PrivateKey  string
	PublicKey   string
	Fingerprint string
	Status      int
	StartAt     *time.Time
	StopAt      *time.Time
	CreatedAt   time.Time
	CreatedBy   *uint
	UpdatedAt   time.Time
	UpdatedBy   *uint
	DeletedAt   *time.Time
	DeletedBy   *uint
	Version     int
}
