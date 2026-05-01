// Package notification は通知ユースケースの入出力データを定義します。
package notification

// ListPageDto は通知一覧取得の入力データです。
type ListPageDto struct {
	// StaffID はスタッフID。
	StaffID uint
	// Cursor はページングカーソル。
	Cursor *string
	// Limit は取得件数上限。
	Limit int
}

// CountsDto は通知件数取得の入力データです。
type CountsDto struct {
	// StaffID はスタッフID。
	StaffID uint
}

// BulkMarkReadDto は通知一括既読の入力データです。
type BulkMarkReadDto struct {
	// StaffID はスタッフID。
	StaffID uint
}

// MarkReadDto は通知個別既読の入力データです。
type MarkReadDto struct {
	// ID は通知ID。
	ID int64
}

// FanOutDto は通知一斉送信の入力データです。
type FanOutDto struct {
	// Title は通知タイトル。
	Title string
	// Message は通知本文。
	Message string
	// MessageType は通知種別。
	MessageType int
	// ExecutorID は操作を実行したスタッフID。
	ExecutorID uint
	// URL は通知に関連するURL。
	URL string
}
