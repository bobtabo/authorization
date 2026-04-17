package notification

// FanOutDto は全スタッフへの通知配信のユースケース入力です。
type FanOutDto struct {
	Title       string
	Message     string
	MessageType int
	ExecutorID  uint
	URL         string
}

