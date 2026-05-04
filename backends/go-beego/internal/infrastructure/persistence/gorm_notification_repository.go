package persistence

import (
	domnotification "authorization-go-beego/internal/domain/notification"
	"authorization-go-beego/internal/infrastructure/model"
	"authorization-go-beego/internal/support"
	"encoding/base64"
	"fmt"
	"strconv"
	"strings"
	"time"
)

type OrmNotificationRepository struct {
	o QueryOrmer
}

func NewOrmNotificationRepository(o QueryOrmer) *OrmNotificationRepository {
	return &OrmNotificationRepository{o: o}
}

func (r *OrmNotificationRepository) ListPage(staffID uint, cursor *string, limit int) ([]*domnotification.Notification, *string, error) {
	sql := "SELECT * FROM notifications WHERE staff_id=? AND deleted_at IS NULL"
	args := []interface{}{staffID}

	if cursor != nil && *cursor != "" {
		ts, cid, err := decodeCursor(*cursor)
		if err == nil {
			sql += " AND (created_at < ? OR (created_at = ? AND id < ?))"
			t := time.Unix(ts, 0)
			args = append(args, t, t, cid)
		}
	}

	sql += " ORDER BY created_at DESC, id DESC LIMIT ?"
	args = append(args, limit+1)

	var ms []*model.Notification
	if _, err := r.o.Raw(sql, args...).QueryRows(&ms); err != nil {
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

func (r *OrmNotificationRepository) Counts(staffID uint) (unread, total int64, err error) {
	total, err = r.o.QueryTable(new(model.Notification)).
		Filter("staff_id", staffID).
		Filter("deleted_at__isnull", true).
		Count()
	if err != nil {
		return
	}
	unread, err = r.o.QueryTable(new(model.Notification)).
		Filter("staff_id", staffID).
		Filter("deleted_at__isnull", true).
		Filter("read", false).
		Count()
	return
}

func (r *OrmNotificationRepository) BulkMarkRead(staffID int64, ids []int64, all bool) (int64, error) {
	now := time.Now()
	if all || len(ids) == 0 {
		res, err := r.o.Raw(
			"UPDATE notifications SET `read`=1, updated_at=? WHERE staff_id=? AND `read`=0 AND deleted_at IS NULL",
			now, staffID,
		).Exec()
		if err != nil {
			return 0, err
		}
		n, _ := res.RowsAffected()
		return n, nil
	}
	placeholders := make([]string, len(ids))
	args := []interface{}{now, staffID}
	for i, id := range ids {
		placeholders[i] = "?"
		args = append(args, id)
	}
	query := "UPDATE notifications SET `read`=1, updated_at=? WHERE staff_id=? AND `read`=0 AND deleted_at IS NULL AND id IN (" + strings.Join(placeholders, ",") + ")"
	res, err := r.o.Raw(query, args...).Exec()
	if err != nil {
		return 0, err
	}
	n, _ := res.RowsAffected()
	return n, nil
}

func (r *OrmNotificationRepository) Store(staffID uint, messageType int, title, message string, createdBy uint, url ...string) error {
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
	_, err := r.o.Insert(&m)
	return err
}

func (r *OrmNotificationRepository) Patch(id int64, attrs map[string]interface{}) (bool, error) {
	now := time.Now()
	if v, ok := attrs["read"]; ok {
		res, err := r.o.Raw(
			"UPDATE notifications SET `read`=?, updated_at=? WHERE id=?",
			v, now, id,
		).Exec()
		if err != nil {
			return false, err
		}
		n, _ := res.RowsAffected()
		return n > 0, nil
	}
	return false, nil
}

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
