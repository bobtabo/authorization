package notification

import "time"

// Notification は通知のドメインエンティティです（GORM タグなし）。
type Notification struct {
	ID          uint64
	StaffID     uint
	MessageType int
	Title       string
	Message     string
	URL         *string
	Read        bool
	CreatedAt   time.Time
	CreatedBy   *uint
	UpdatedAt   time.Time
	UpdatedBy   *uint
	DeletedAt   *time.Time
	DeletedBy   *uint
	Version     int
}
