package client

import "time"

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
