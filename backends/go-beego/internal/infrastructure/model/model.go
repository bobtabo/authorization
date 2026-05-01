package model

import "time"

type Client struct {
	ID          uint64     `orm:"pk;auto;column(id)"`
	Name        string     `orm:"column(name)"`
	Identifier  string     `orm:"column(identifier)"`
	PostCode    string     `orm:"column(post_code)"`
	Pref        string     `orm:"column(pref)"`
	City        string     `orm:"column(city)"`
	Address     string     `orm:"column(address)"`
	Building    string     `orm:"column(building)"`
	Tel         string     `orm:"column(tel)"`
	Email       string     `orm:"column(email)"`
	AccessToken string     `orm:"column(access_token)"`
	PrivateKey  string     `orm:"column(private_key)"`
	PublicKey   string     `orm:"column(public_key)"`
	Fingerprint string     `orm:"column(fingerprint)"`
	Status      int        `orm:"column(status)"`
	StartAt     *time.Time `orm:"column(start_at);null"`
	StopAt      *time.Time `orm:"column(stop_at);null"`
	CreatedAt   time.Time  `orm:"column(created_at)"`
	CreatedBy   *uint      `orm:"column(created_by);null"`
	UpdatedAt   time.Time  `orm:"column(updated_at)"`
	UpdatedBy   *uint      `orm:"column(updated_by);null"`
	DeletedAt   *time.Time `orm:"column(deleted_at);null"`
	DeletedBy   *uint      `orm:"column(deleted_by);null"`
	Version     int        `orm:"column(version)"`
}

func (c *Client) TableName() string { return "clients" }

type Staff struct {
	ID          uint       `orm:"pk;auto;column(id)"`
	Name        string     `orm:"column(name)"`
	Email       string     `orm:"column(email)"`
	Provider    int        `orm:"column(provider)"`
	ProviderID  string     `orm:"column(provider_id)"`
	Avatar      *string    `orm:"column(avatar);null"`
	Role        int        `orm:"column(role)"`
	LastLoginAt *time.Time `orm:"column(last_login_at);null"`
	CreatedAt   time.Time  `orm:"column(created_at)"`
	CreatedBy   *uint      `orm:"column(created_by);null"`
	UpdatedAt   time.Time  `orm:"column(updated_at)"`
	UpdatedBy   *uint      `orm:"column(updated_by);null"`
	DeletedAt   *time.Time `orm:"column(deleted_at);null"`
	DeletedBy   *uint      `orm:"column(deleted_by);null"`
	Version     int        `orm:"column(version)"`
}

func (s *Staff) TableName() string { return "staffs" }

type Invitation struct {
	ID        uint       `orm:"pk;auto;column(id)"`
	Token     string     `orm:"column(token)"`
	CreatedAt time.Time  `orm:"column(created_at)"`
	CreatedBy *uint      `orm:"column(created_by);null"`
	UpdatedAt time.Time  `orm:"column(updated_at)"`
	UpdatedBy *uint      `orm:"column(updated_by);null"`
	DeletedAt *time.Time `orm:"column(deleted_at);null"`
	DeletedBy *uint      `orm:"column(deleted_by);null"`
	Version   int        `orm:"column(version)"`
}

func (i *Invitation) TableName() string { return "invitations" }

type Notification struct {
	ID          uint64     `orm:"pk;auto;column(id)"`
	StaffID     uint       `orm:"column(staff_id)"`
	MessageType int        `orm:"column(message_type)"`
	Title       string     `orm:"column(title)"`
	Message     string     `orm:"column(message)"`
	URL         *string    `orm:"column(url);null"`
	Read        bool       `orm:"column(read)"`
	CreatedAt   time.Time  `orm:"column(created_at)"`
	CreatedBy   *uint      `orm:"column(created_by);null"`
	UpdatedAt   time.Time  `orm:"column(updated_at)"`
	UpdatedBy   *uint      `orm:"column(updated_by);null"`
	DeletedAt   *time.Time `orm:"column(deleted_at);null"`
	DeletedBy   *uint      `orm:"column(deleted_by);null"`
	Version     int        `orm:"column(version)"`
}

func (n *Notification) TableName() string { return "notifications" }
