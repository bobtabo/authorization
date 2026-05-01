// Package notification は通知ユースケースを提供します。
package notification

import (
	domnotification "authorization-go-echo/internal/domain/notification"
	domstaff "authorization-go-echo/internal/domain/staff"
)

// Interactor は通知ユースケースの実装です。
type Interactor struct {
	repo      domnotification.Repository
	staffRepo domstaff.Repository
}

// NewInteractor は Interactor を生成します。
func NewInteractor(repo domnotification.Repository, staffRepo domstaff.Repository) *Interactor {
	return &Interactor{repo: repo, staffRepo: staffRepo}
}

// ListPage は通知一覧をカーソルページングで取得します。
func (uc *Interactor) ListPage(dto ListPageDto) (*domnotification.Page, error) {
	limit := dto.Limit
	if limit < 1 {
		limit = 1
	}
	if limit > 100 {
		limit = 100
	}
	notifications, nextCursor, err := uc.repo.ListPage(dto.StaffID, dto.Cursor, limit)
	if err != nil {
		return nil, err
	}
	items := make([]*domnotification.Item, 0, len(notifications))
	for _, n := range notifications {
		items = append(items, notificationToItem(n))
	}
	return &domnotification.Page{Items: items, NextCursor: nextCursor}, nil
}

// Counts は未読件数と総件数を返します。
func (uc *Interactor) Counts(dto CountsDto) (unread, total int64, err error) {
	return uc.repo.Counts(dto.StaffID)
}

// BulkMarkRead は全通知を一括既読にします。
func (uc *Interactor) BulkMarkRead(dto BulkMarkReadDto) (int64, error) {
	return uc.repo.BulkMarkRead(int64(dto.StaffID), nil, true)
}

// FanOut は全アクティブスタッフに通知を一斉送信します。
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

// MarkRead は指定した通知を既読にします。
func (uc *Interactor) MarkRead(dto MarkReadDto) error {
	_, err := uc.repo.Patch(dto.ID, map[string]interface{}{"read": true})
	return err
}

func notificationToItem(n *domnotification.Notification) *domnotification.Item {
	return &domnotification.Item{
		ID:          n.ID,
		StaffID:     n.StaffID,
		MessageType: n.MessageType,
		Title:       n.Title,
		Message:     n.Message,
		URL:         n.URL,
		Read:        n.Read,
		CreatedAt:   n.CreatedAt.Format("2006-01-02 15:04"),
		UpdatedAt:   n.UpdatedAt.Format("2006-01-02 15:04"),
	}
}
