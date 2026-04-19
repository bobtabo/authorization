package client

import "time"

// ListItem はクライアント一覧レスポンス用の値オブジェクトです。
type ListItem struct {
	ID        uint64
	Name      string
	Status    int
	StartAt   *time.Time
	StopAt    *time.Time
	CreatedAt time.Time
	UpdatedAt time.Time
}

// DetailVo はクライアント詳細レスポンス用の値オブジェクトです。
type DetailVo struct {
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
	Status      int
	StartAt     *time.Time
	StopAt      *time.Time
	CreatedAt   time.Time
	UpdatedAt   time.Time
}
