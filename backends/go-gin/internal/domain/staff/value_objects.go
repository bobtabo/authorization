package staff

import "time"

// ListItem はスタッフ一覧レスポンス用の値オブジェクトです。
type ListItem struct {
	ID        uint
	Name      string
	Email     string
	Role      int
	Status    int // 0=削除済み, 1=有効
	CreatedAt time.Time
	UpdatedAt time.Time
}

// Vo はスタッフ詳細レスポンス用の値オブジェクトです。
type Vo struct {
	ID     uint
	Name   string
	Avatar *string
	Role   int
}
