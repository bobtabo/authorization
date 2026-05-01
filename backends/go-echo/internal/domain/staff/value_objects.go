package staff

import "time"

type ListItem struct {
	ID        uint
	Name      string
	Email     string
	Role      int
	Status    int
	CreatedAt time.Time
	UpdatedAt time.Time
}

type Vo struct {
	ID     uint
	Name   string
	Avatar *string
	Role   int
}
