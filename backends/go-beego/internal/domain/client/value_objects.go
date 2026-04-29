package client

import "time"

type ListItem struct {
	ID        uint64
	Name      string
	Status    int
	StartAt   *time.Time
	StopAt    *time.Time
	CreatedAt time.Time
	UpdatedAt time.Time
}

type DetailVo struct {
	ID        uint64
	Name      string
	Identifier string
	PostCode  string
	Pref      string
	City      string
	Address   string
	Building  string
	Tel       string
	Email     string
	Status    int
	StartAt   *time.Time
	StopAt    *time.Time
	CreatedAt time.Time
	UpdatedAt time.Time
}

type StoreVo struct {
	ID          uint64
	Name        string
	Email       string
	AccessToken string
}
