package notification

type Repository interface {
	ListPage(staffID uint, cursor *string, limit int) ([]*Notification, *string, error)
	Counts(staffID uint) (unread, total int64, err error)
	BulkMarkRead(staffID int64, ids []int64, all bool) (int64, error)
	Store(staffID uint, messageType int, title, message string, createdBy uint, url ...string) error
	Patch(id int64, attrs map[string]interface{}) (bool, error)
}
