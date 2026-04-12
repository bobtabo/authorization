package repository

import (
	"authorization-go/internal/model"
	"encoding/base64"
	"fmt"
	"strconv"
	"strings"
	"time"

	"gorm.io/gorm"
)

type NotificationRepository struct {
	db *gorm.DB
}

func NewNotificationRepository(db *gorm.DB) *NotificationRepository {
	return &NotificationRepository{db: db}
}

type NotificationPage struct {
	Items      []*model.Notification
	NextCursor *string
}

// ListPage はカーソルページングで通知一覧を返します。
// cursor = base64(unix_timestamp,id) 形式
func (r *NotificationRepository) ListPage(staffID uint, cursor *string, limit int) (*NotificationPage, error) {
	q := r.db.Where("staff_id = ?", staffID).Order("created_at DESC, id DESC")

	if cursor != nil && *cursor != "" {
		ts, cid, err := decodeCursor(*cursor)
		if err == nil {
			q = q.Where("(created_at < ? OR (created_at = ? AND id < ?))",
				time.Unix(ts, 0), time.Unix(ts, 0), cid)
		}
	}

	var items []*model.Notification
	if err := q.Limit(limit + 1).Find(&items).Error; err != nil {
		return nil, err
	}

	var nextCursor *string
	if len(items) > limit {
		items = items[:limit]
		last := items[len(items)-1]
		c := encodeCursor(last.CreatedAt.Unix(), int64(last.ID))
		nextCursor = &c
	}

	return &NotificationPage{Items: items, NextCursor: nextCursor}, nil
}

func (r *NotificationRepository) Counts(staffID uint) (unread, total int64, err error) {
	r.db.Model(&model.Notification{}).Where("staff_id = ?", staffID).Count(&total)
	r.db.Model(&model.Notification{}).Where("staff_id = ? AND read = false", staffID).Count(&unread)
	return
}

// BulkMarkRead は指定条件の通知を既読にして更新件数を返します。
func (r *NotificationRepository) BulkMarkRead(staffID int64, ids []int64, all bool) (int64, error) {
	q := r.db.Model(&model.Notification{}).Where("staff_id = ? AND read = false", staffID)
	if !all && len(ids) > 0 {
		q = q.Where("id IN ?", ids)
	}
	result := q.Updates(map[string]interface{}{"read": true, "updated_at": time.Now()})
	return result.RowsAffected, result.Error
}

// Store は新規通知を1件保存します。
func (r *NotificationRepository) Store(staffID uint, messageType int, title, message string, createdBy uint) error {
	now := time.Now()
	n := model.Notification{
		StaffID:     staffID,
		MessageType: messageType,
		Title:       title,
		Message:     message,
		Read:        false,
		CreatedAt:   now,
		UpdatedAt:   now,
		CreatedBy:   &createdBy,
	}
	return r.db.Create(&n).Error
}

// Patch は通知を部分更新します。対応フィールドは read のみ。
func (r *NotificationRepository) Patch(id int64, attrs map[string]interface{}) (bool, error) {
	updates := map[string]interface{}{"updated_at": time.Now()}
	if v, ok := attrs["read"]; ok {
		updates["read"] = v
	}
	result := r.db.Model(&model.Notification{}).Where("id = ?", id).Updates(updates)
	return result.RowsAffected > 0, result.Error
}

func encodeCursor(unixSec, id int64) string {
	raw := fmt.Sprintf("%d,%d", unixSec, id)
	return base64.StdEncoding.EncodeToString([]byte(raw))
}

func decodeCursor(cursor string) (unixSec, id int64, err error) {
	b, err := base64.StdEncoding.DecodeString(cursor)
	if err != nil {
		return
	}
	parts := strings.SplitN(string(b), ",", 2)
	if len(parts) != 2 {
		err = fmt.Errorf("invalid cursor")
		return
	}
	unixSec, err = strconv.ParseInt(parts[0], 10, 64)
	if err != nil {
		return
	}
	id, err = strconv.ParseInt(parts[1], 10, 64)
	return
}
