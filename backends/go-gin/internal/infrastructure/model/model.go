package model

import (
	"time"

	"gorm.io/gorm"
)

// Client は clients テーブルの GORM モデルです。
type Client struct {
	ID          uint64         `gorm:"primaryKey;autoIncrement;column:id"`
	Name        string         `gorm:"not null;column:name"`
	Identifier  string         `gorm:"not null;uniqueIndex;column:identifier"`
	PostCode    string         `gorm:"column:post_code"`
	Pref        string         `gorm:"column:pref"`
	City        string         `gorm:"column:city"`
	Address     string         `gorm:"column:address"`
	Building    string         `gorm:"column:building"`
	Tel         string         `gorm:"column:tel"`
	Email       string         `gorm:"column:email"`
	AccessToken string         `gorm:"not null;uniqueIndex;column:access_token"`
	PrivateKey  string         `gorm:"not null;column:private_key"`
	PublicKey   string         `gorm:"not null;column:public_key"`
	Fingerprint string         `gorm:"not null;column:fingerprint"`
	Status      int            `gorm:"not null;default:1;column:status"`
	StartAt     *time.Time     `gorm:"column:start_at"`
	StopAt      *time.Time     `gorm:"column:stop_at"`
	CreatedAt   time.Time      `gorm:"column:created_at"`
	CreatedBy   *uint          `gorm:"column:created_by"`
	UpdatedAt   time.Time      `gorm:"column:updated_at"`
	UpdatedBy   *uint          `gorm:"column:updated_by"`
	DeletedAt   gorm.DeletedAt `gorm:"index;column:deleted_at"`
	DeletedBy   *uint          `gorm:"column:deleted_by"`
	Version     int            `gorm:"default:0;column:version"`
}

func (Client) TableName() string { return "clients" }

// Staff は staffs テーブルの GORM モデルです。
type Staff struct {
	ID          uint           `gorm:"primaryKey;autoIncrement;column:id"`
	Name        string         `gorm:"not null;column:name"`
	Email       string         `gorm:"not null;uniqueIndex;column:email"`
	Provider    int            `gorm:"not null;column:provider"`
	ProviderID  string         `gorm:"not null;column:provider_id"`
	Avatar      *string        `gorm:"column:avatar"`
	Role        int            `gorm:"not null;default:2;column:role"`
	LastLoginAt *time.Time     `gorm:"column:last_login_at"`
	CreatedAt   time.Time      `gorm:"column:created_at"`
	CreatedBy   *uint          `gorm:"column:created_by"`
	UpdatedAt   time.Time      `gorm:"column:updated_at"`
	UpdatedBy   *uint          `gorm:"column:updated_by"`
	DeletedAt   gorm.DeletedAt `gorm:"index;column:deleted_at"`
	DeletedBy   *uint          `gorm:"column:deleted_by"`
	Version     int            `gorm:"default:0;column:version"`
}

func (Staff) TableName() string { return "staffs" }

// Invitation は invitations テーブルの GORM モデルです。
type Invitation struct {
	ID        uint           `gorm:"primaryKey;autoIncrement;column:id"`
	Token     string         `gorm:"not null;uniqueIndex;column:token"`
	CreatedAt time.Time      `gorm:"column:created_at"`
	CreatedBy *uint          `gorm:"column:created_by"`
	UpdatedAt time.Time      `gorm:"column:updated_at"`
	UpdatedBy *uint          `gorm:"column:updated_by"`
	DeletedAt gorm.DeletedAt `gorm:"index;column:deleted_at"`
	DeletedBy *uint          `gorm:"column:deleted_by"`
	Version   int            `gorm:"default:0;column:version"`
}

func (Invitation) TableName() string { return "invitations" }

// Notification は notifications テーブルの GORM モデルです。
type Notification struct {
	ID          uint64         `gorm:"primaryKey;autoIncrement;column:id"`
	StaffID     uint           `gorm:"not null;index;column:staff_id"`
	MessageType int            `gorm:"not null;column:message_type"`
	Title       string         `gorm:"not null;column:title"`
	Message     string         `gorm:"not null;column:message"`
	URL         *string        `gorm:"column:url"`
	Read        bool           `gorm:"not null;default:false;column:read"`
	CreatedAt   time.Time      `gorm:"column:created_at"`
	CreatedBy   *uint          `gorm:"column:created_by"`
	UpdatedAt   time.Time      `gorm:"column:updated_at"`
	UpdatedBy   *uint          `gorm:"column:updated_by"`
	DeletedAt   gorm.DeletedAt `gorm:"index;column:deleted_at"`
	DeletedBy   *uint          `gorm:"column:deleted_by"`
	Version     int            `gorm:"default:1;column:version"`
}

func (Notification) TableName() string { return "notifications" }
