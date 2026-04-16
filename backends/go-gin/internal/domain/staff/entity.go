package staff

import "time"

// Staff はスタッフのドメインエンティティです（GORM タグなし）。
type Staff struct {
	ID          uint
	Name        string
	Email       string
	Provider    int
	ProviderID  string
	Avatar      *string
	Role        int
	LastLoginAt *time.Time
	CreatedAt   time.Time
	CreatedBy   *uint
	UpdatedAt   time.Time
	UpdatedBy   *uint
	DeletedAt   *time.Time
	DeletedBy   *uint
	Version     int
}
