package notification

// Repository は通知の永続化インターフェースです。
type Repository interface {
	// ListPage はカーソルページングで通知エンティティ一覧とカーソルを返します。
	ListPage(staffID uint, cursor *string, limit int) ([]*Notification, *string, error)
	// Counts は未読数と全件数を返します。
	Counts(staffID uint) (unread, total int64, err error)
	// BulkMarkRead は条件に一致する通知を既読にして更新件数を返します。
	BulkMarkRead(staffID int64, ids []int64, all bool) (int64, error)
	// Store は新規通知を1件保存します。
	Store(staffID uint, messageType int, title, message string, createdBy uint, url ...string) error
	// Patch は通知を部分更新します。更新があった場合 true を返します。
	Patch(id int64, attrs map[string]interface{}) (bool, error)
}
