package client

import "time"

// Condition はクライアント検索条件です。
type Condition struct {
	Keyword   *string
	StartFrom *time.Time
	StartTo   *time.Time
	Statuses  []int
	Offset    int
	Limit     int
	Sort      string
	SortType  string
}
