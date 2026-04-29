package notification

type FanOutDto struct {
	Title       string
	Message     string
	MessageType int
	ExecutorID  uint
	URL         string
}
