package notification

type Page struct {
	Items      []*Item
	NextCursor *string
}

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
