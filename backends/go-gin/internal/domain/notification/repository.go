package notification

// Repository は通知の永続化インターフェースです。
type Repository interface {
	ListPage(staffID uint, cursor *string, limit int) (*Page, error)
	Counts(staffID uint) (unread, total int64, err error)
	BulkMarkRead(staffID int64, ids []int64, all bool) (int64, error)
	Store(staffID uint, messageType int, title, message string, createdBy uint, url ...string) error
	Patch(id int64, attrs map[string]interface{}) (bool, error)
}
