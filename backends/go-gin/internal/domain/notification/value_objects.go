package notification

// Page はカーソルページングの結果です。
type Page struct {
	Items      []*Notification
	NextCursor *string
}

// Item は通知一覧レスポンス用の値オブジェクトです（MapNotification の出力先）。
type Item struct {
	ID          uint64
	StaffID     uint
	MessageType int
	Title       string
	Message     string
	URL         *string
	Read        bool
	CreatedAt   string
	UpdatedAt   string
}
