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

// StoreVo はクライアント登録結果の値オブジェクトです。
// メール送信・通知配信に必要なフィールドを含みます。
type StoreVo struct {
	ID          uint64
	Name        string
	Email       string
	AccessToken string
}

// QrVo はQRコードデータのレスポンス用値オブジェクトです。
type QrVo struct {
	Identifier  string
	DeeplinkURL string
}

// InfoVo はスマホアプリ向けクライアント情報のレスポンス用値オブジェクトです。
type InfoVo struct {
	Identifier string
	Name       string
	Status     int
}

// StartVo は利用開始レスポンス用値オブジェクトです。
type StartVo struct {
	AccessToken string
}
