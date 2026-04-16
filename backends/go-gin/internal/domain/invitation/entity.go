package invitation

import "time"

// Invitation は招待のドメインエンティティです（GORM タグなし）。
type Invitation struct {
	ID        uint
	Token     string
	CreatedAt time.Time
	CreatedBy *uint
	UpdatedAt time.Time
	UpdatedBy *uint
	DeletedAt *time.Time
	DeletedBy *uint
	Version   int
}
