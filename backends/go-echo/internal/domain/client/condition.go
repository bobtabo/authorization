package client

import "time"

type Condition struct {
	Keyword   *string
	StartFrom *time.Time
	StartTo   *time.Time
	Statuses  []int
}
