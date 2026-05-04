package persistence

import (
	"authorization-go-echo/ent"
	"authorization-go-echo/ent/notification"
	domnotification "authorization-go-echo/internal/domain/notification"
	"authorization-go-echo/internal/support"
	"context"
	"encoding/base64"
	"fmt"
	"strconv"
	"strings"
	"time"
)

type EntNotificationRepository struct {
	db *ent.Client
}

func NewEntNotificationRepository(db *ent.Client) *EntNotificationRepository {
	return &EntNotificationRepository{db: db}
}

func (r *EntNotificationRepository) ListPage(staffID uint, cursor *string, limit int) ([]*domnotification.Notification, *string, error) {
	ctx := context.Background()
	q := r.db.Notification.Query().
		Where(notification.StaffIDEQ(staffID), notification.DeletedAtIsNil()).
		Order(ent.Desc(notification.FieldCreatedAt), ent.Desc(notification.FieldID))

	if cursor != nil && *cursor != "" {
		ts, cid, err := decodeEntCursor(*cursor)
		if err == nil {
			t := time.Unix(ts, 0)
			q = q.Where(notification.Or(
				notification.CreatedAtLT(t),
				notification.And(
					notification.CreatedAtEQ(t),
					notification.IDLT(uint64(cid)),
				),
			))
		}
	}

	ms, err := q.Limit(limit + 1).All(ctx)
	if err != nil {
		return nil, nil, err
	}

	var nextCursor *string
	if len(ms) > limit {
		ms = ms[:limit]
		last := ms[len(ms)-1]
		c := encodeEntCursor(last.CreatedAt.Unix(), int64(last.ID))
		nextCursor = &c
	}

	notifications := make([]*domnotification.Notification, 0, len(ms))
	for _, m := range ms {
		notifications = append(notifications, entNotificationToDomain(m))
	}
	return notifications, nextCursor, nil
}

func (r *EntNotificationRepository) Counts(staffID uint) (unread, total int64, err error) {
	ctx := context.Background()
	var n int
	n, err = r.db.Notification.Query().
		Where(notification.StaffIDEQ(staffID), notification.DeletedAtIsNil()).
		Count(ctx)
	if err != nil {
		return
	}
	total = int64(n)
	n, err = r.db.Notification.Query().
		Where(notification.StaffIDEQ(staffID), notification.DeletedAtIsNil(), notification.ReadEQ(false)).
		Count(ctx)
	unread = int64(n)
	return
}

func (r *EntNotificationRepository) BulkMarkRead(staffID int64, ids []int64, all bool) (int64, error) {
	ctx := context.Background()
	now := time.Now()
	q := r.db.Notification.Update().
		Where(notification.StaffIDEQ(uint(staffID)), notification.ReadEQ(false), notification.DeletedAtIsNil()).
		SetRead(true).
		SetUpdatedAt(now)
	if !all && len(ids) > 0 {
		uint64IDs := make([]uint64, len(ids))
		for i, id := range ids {
			uint64IDs[i] = uint64(id)
		}
		q = q.Where(notification.IDIn(uint64IDs...))
	}
	n, err := q.Save(ctx)
	return int64(n), err
}

func (r *EntNotificationRepository) Store(staffID uint, messageType int, title, message string, createdBy uint, url ...string) error {
	now := time.Now()
	q := r.db.Notification.Create().
		SetStaffID(staffID).
		SetMessageType(messageType).
		SetTitle(title).
		SetMessage(message).
		SetRead(false).
		SetCreatedAt(now).
		SetCreatedBy(createdBy).
		SetUpdatedAt(now).
		SetUpdatedBy(createdBy)
	if len(url) > 0 && url[0] != "" {
		q = q.SetURL(url[0])
	}
	_, err := q.Save(context.Background())
	return err
}

func (r *EntNotificationRepository) Patch(id int64, attrs map[string]interface{}) (bool, error) {
	now := time.Now()
	q := r.db.Notification.UpdateOneID(uint64(id)).SetUpdatedAt(now)
	if v, ok := attrs["read"]; ok {
		q = q.SetRead(v.(bool))
	}
	m, err := q.Save(context.Background())
	if err != nil {
		if ent.IsNotFound(err) {
			return false, nil
		}
		return false, err
	}
	_ = m
	return true, nil
}

func entNotificationToDomain(m *ent.Notification) *domnotification.Notification {
	n := &domnotification.Notification{}
	support.Assign(n, m)
	return n
}

func encodeEntCursor(unixSec, id int64) string {
	raw := fmt.Sprintf("%d,%d", unixSec, id)
	return base64.StdEncoding.EncodeToString([]byte(raw))
}

func decodeEntCursor(cursor string) (unixSec, id int64, err error) {
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
