// Package persistence はインフラストラクチャ層の GORM リポジトリ実装を提供します。
package persistence

import (
	domnotification "authorization-go/internal/domain/notification"
	"authorization-go/internal/infrastructure/model"
	"authorization-go/internal/support"
	"encoding/base64"
	"fmt"
	"strconv"
	"strings"
	"time"

	"gorm.io/gorm"
)

// GormNotificationRepository は domain/notification.Repository の GORM 実装です。
type GormNotificationRepository struct {
	db *gorm.DB
}

// NewGormNotificationRepository は GormNotificationRepository を生成します。
//
// db: GORM DB インスタンス
func NewGormNotificationRepository(db *gorm.DB) *GormNotificationRepository {
	return &GormNotificationRepository{db: db}
}

// ListPage はカーソルページングで通知エンティティ一覧とカーソルを返します。
// cursor は base64(unix_timestamp,id) 形式で、nil または空文字の場合は先頭から取得します。
//
// staffID: スタッフID
// cursor: ページカーソル
// limit: 取得件数上限
// 戻り値: 通知エンティティのスライス、次ページカーソル、またはエラー
func (r *GormNotificationRepository) ListPage(staffID uint, cursor *string, limit int) ([]*domnotification.Notification, *string, error) {
	q := r.db.Where("staff_id = ?", staffID).Order("created_at DESC, id DESC")

	if cursor != nil && *cursor != "" {
		ts, cid, err := decodeCursor(*cursor)
		if err == nil {
			q = q.Where("(created_at < ? OR (created_at = ? AND id < ?))",
				time.Unix(ts, 0), time.Unix(ts, 0), cid)
		}
	}

	var ms []*model.Notification
	if err := q.Limit(limit + 1).Find(&ms).Error; err != nil {
		return nil, nil, err
	}

	var nextCursor *string
	if len(ms) > limit {
		ms = ms[:limit]
		last := ms[len(ms)-1]
		c := encodeCursor(last.CreatedAt.Unix(), int64(last.ID))
		nextCursor = &c
	}

	notifications := make([]*domnotification.Notification, 0, len(ms))
	for _, m := range ms {
		notifications = append(notifications, notificationToDomain(m))
	}
	return notifications, nextCursor, nil
}

func (r *GormNotificationRepository) Counts(staffID uint) (unread, total int64, err error) {
	r.db.Model(&model.Notification{}).Where("staff_id = ?", staffID).Count(&total)
	r.db.Model(&model.Notification{}).Where("staff_id = ? AND `read` = false", staffID).Count(&unread)
	return
}

// BulkMarkRead は指定条件の通知を既読にして更新件数を返します。
func (r *GormNotificationRepository) BulkMarkRead(staffID int64, ids []int64, all bool) (int64, error) {
	q := r.db.Model(&model.Notification{}).Where("staff_id = ? AND `read` = false", staffID)
	if !all && len(ids) > 0 {
		q = q.Where("id IN ?", ids)
	}
	result := q.Updates(map[string]interface{}{"read": true, "updated_at": time.Now()})
	return result.RowsAffected, result.Error
}

// Store は新規通知を1件保存します。
func (r *GormNotificationRepository) Store(staffID uint, messageType int, title, message string, createdBy uint, url ...string) error {
	now := time.Now()
	m := model.Notification{
		StaffID:     staffID,
		MessageType: messageType,
		Title:       title,
		Message:     message,
		Read:        false,
		CreatedAt:   now,
		UpdatedAt:   now,
		CreatedBy:   &createdBy,
		UpdatedBy:   &createdBy,
	}
	if len(url) > 0 && url[0] != "" {
		m.URL = &url[0]
	}
	return r.db.Create(&m).Error
}

// Patch は通知を部分更新します。対応フィールドは read のみ。
func (r *GormNotificationRepository) Patch(id int64, attrs map[string]interface{}) (bool, error) {
	updates := map[string]interface{}{"updated_at": time.Now()}
	if v, ok := attrs["read"]; ok {
		updates["read"] = v
	}
	result := r.db.Model(&model.Notification{}).Where("id = ?", id).Updates(updates)
	return result.RowsAffected > 0, result.Error
}

// ---------- マッピングヘルパー ----------

func notificationToDomain(m *model.Notification) *domnotification.Notification {
	n := &domnotification.Notification{}
	support.Assign(n, m)
	return n
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
