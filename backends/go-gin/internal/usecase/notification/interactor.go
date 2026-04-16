package notification

import (
	domnotification "authorization-go/internal/domain/notification"
	domstaff "authorization-go/internal/domain/staff"
)

// Interactor は通知のユースケースを実装します。
type Interactor struct {
	repo      domnotification.Repository
	staffRepo domstaff.Repository
}

func NewInteractor(repo domnotification.Repository, staffRepo domstaff.Repository) *Interactor {
	return &Interactor{repo: repo, staffRepo: staffRepo}
}

// ListPage はカーソルページングで通知一覧を返します。
func (uc *Interactor) ListPage(staffID uint, cursor *string, limit int) (*domnotification.Page, error) {
	if limit < 1 {
		limit = 1
	}
	if limit > 100 {
		limit = 100
	}
	return uc.repo.ListPage(staffID, cursor, limit)
}

// Counts はスタッフの未読・全体通知数を返します。
func (uc *Interactor) Counts(staffID uint) (unread, total int64, err error) {
	return uc.repo.Counts(staffID)
}

// BulkMarkRead は指定条件の通知を既読にして更新件数を返します。
func (uc *Interactor) BulkMarkRead(dto BulkMarkReadDto) (int64, error) {
	return uc.repo.BulkMarkRead(dto.StaffID, dto.IDs, dto.All)
}

// FanOut は全アクティブスタッフへ通知を配信します。
func (uc *Interactor) FanOut(dto FanOutDto) error {
	staffs, err := uc.staffRepo.FindAllActive()
	if err != nil {
		return err
	}
	for _, s := range staffs {
		_ = uc.repo.Store(s.ID, dto.MessageType, dto.Title, dto.Message, dto.ExecutorID, dto.URL)
	}
	return nil
}

// Patch は通知を部分更新します。
func (uc *Interactor) Patch(id int64, attrs map[string]interface{}) error {
	_, err := uc.repo.Patch(id, attrs)
	return err
}

// MapNotification は domain Notification をレスポンス用マップに変換します。
func MapNotification(n *domnotification.Notification) map[string]interface{} {
	return map[string]interface{}{
		"id":           n.ID,
		"staff_id":     n.StaffID,
		"message_type": n.MessageType,
		"title":        n.Title,
		"message":      n.Message,
		"url":          n.URL,
		"read":         n.Read,
		"created_at":   n.CreatedAt.Format("2006-01-02 15:04"),
		"updated_at":   n.UpdatedAt.Format("2006-01-02 15:04"),
	}
}
