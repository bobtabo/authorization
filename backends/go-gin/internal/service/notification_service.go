package service

import (
	"authorization-go/internal/model"
	"authorization-go/internal/repository"
)

type NotificationService struct {
	repo      *repository.NotificationRepository
	staffRepo *repository.StaffRepository
}

func NewNotificationService(
	repo *repository.NotificationRepository,
	staffRepo *repository.StaffRepository,
) *NotificationService {
	return &NotificationService{repo: repo, staffRepo: staffRepo}
}

func (s *NotificationService) ListPage(staffID uint, cursor *string, limit int) (*repository.NotificationPage, error) {
	if limit < 1 {
		limit = 1
	}
	if limit > 100 {
		limit = 100
	}
	return s.repo.ListPage(staffID, cursor, limit)
}

func (s *NotificationService) Counts(staffID uint) (unread, total int64, err error) {
	return s.repo.Counts(staffID)
}

func (s *NotificationService) BulkMarkRead(staffID int64, ids []int64, all bool) (int64, error) {
	return s.repo.BulkMarkRead(staffID, ids, all)
}

// FanOut は全アクティブスタッフへ通知を配信します。
func (s *NotificationService) FanOut(title, message string, messageType int, executorID uint) error {
	staffs, err := s.staffRepo.FindAllActive()
	if err != nil {
		return err
	}
	for _, staff := range staffs {
		_ = s.repo.Store(staff.ID, messageType, title, message, executorID)
	}
	return nil
}

func (s *NotificationService) Patch(id int64, attrs map[string]interface{}) error {
	ok, err := s.repo.Patch(id, attrs)
	if err != nil {
		return err
	}
	if !ok {
		return nil // not found は無視
	}
	return nil
}

// MapNotification は model.Notification をレスポンス用マップに変換します。
func MapNotification(n *model.Notification) map[string]interface{} {
	return map[string]interface{}{
		"id":           n.ID,
		"staff_id":     n.StaffID,
		"message_type": n.MessageType,
		"title":        n.Title,
		"message":      n.Message,
		"read":         n.Read,
		"created_at":   n.CreatedAt.Format("2006-01-02 15:04"),
		"updated_at":   n.UpdatedAt.Format("2006-01-02 15:04"),
	}
}
