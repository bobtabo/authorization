package notification

// FanOutDto は全スタッフへの通知配信のユースケース入力です。
type FanOutDto struct {
	Title       string
	Message     string
	MessageType int
	ExecutorID  uint
	URL         string
}

// BulkMarkReadDto は一括既読更新のユースケース入力です。
type BulkMarkReadDto struct {
	StaffID int64
	IDs     []int64
	All     bool
}
