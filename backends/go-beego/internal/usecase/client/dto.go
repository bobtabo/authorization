// Package client はクライアントユースケースの入出力データを定義します。
package client

// FindByIDDto はIDによるクライアント取得の入力データです。
type FindByIDDto struct {
	// ID はクライアントID。
	ID uint64
}

// DestroyDto はクライアント削除の入力データです。
type DestroyDto struct {
	// ID はクライアントID。
	ID uint64
	// ExecutorID は操作を実行したスタッフID。
	ExecutorID uint
}

// StoreDto はクライアント登録の入力データです。
type StoreDto struct {
	// Name はクライアント名。
	Name string
	// PostCode は郵便番号。
	PostCode string
	// Pref は都道府県。
	Pref string
	// City は市区町村。
	City string
	// Address は番地。
	Address string
	// Building は建物名。
	Building string
	// Tel は電話番号。
	Tel string
	// Email はメールアドレス。
	Email string
	// ExecutorID は操作を実行したスタッフID。
	ExecutorID uint
}

// UpdateDto はクライアント更新の入力データです。
type UpdateDto struct {
	// ID はクライアントID。
	ID uint64
	// Name はクライアント名。
	Name *string
	// PostCode は郵便番号。
	PostCode *string
	// Pref は都道府県。
	Pref *string
	// City は市区町村。
	City *string
	// Address は番地。
	Address *string
	// Building は建物名。
	Building *string
	// Tel は電話番号。
	Tel *string
	// Email はメールアドレス。
	Email *string
	// Status はステータス。
	Status *int
	// ExecutorID は操作を実行したスタッフID。
	ExecutorID uint
}

// ListConditionDto はクライアント一覧検索の入力データです。
type ListConditionDto struct {
	// Keyword は検索キーワード。
	Keyword *string
	// StartFrom はサービス開始日の下限。
	StartFrom *string
	// StartTo はサービス開始日の上限。
	StartTo *string
	// Statuses はステータスの絞り込みリスト。
	Statuses []int
}
