package notification

import (
	domnotification "authorization-go-echo/internal/domain/notification"
	domstaff "authorization-go-echo/internal/domain/staff"
)

type Interactor struct {
	repo      domnotification.Repository
	staffRepo domstaff.Repository
}

func NewInteractor(repo domnotification.Repository, staffRepo domstaff.Repository) *Interactor {
	return &Interactor{repo: repo, staffRepo: staffRepo}
}

func (uc *Interactor) ListPage(staffID uint, cursor *string, limit int) (*domnotification.Page, error) {
	if limit < 1 {
		limit = 1
	}
	if limit > 100 {
		limit = 100
	}
	notifications, nextCursor, err := uc.repo.ListPage(staffID, cursor, limit)
	if err != nil {
		return nil, err
	}
	items := make([]*domnotification.Item, 0, len(notifications))
	for _, n := range notifications {
		items = append(items, notificationToItem(n))
	}
	return &domnotification.Page{Items: items, NextCursor: nextCursor}, nil
}

func (uc *Interactor) Counts(staffID uint) (unread, total int64, err error) {
	return uc.repo.Counts(staffID)
}

func (uc *Interactor) BulkMarkRead(staffID uint) (int64, error) {
	return uc.repo.BulkMarkRead(int64(staffID), nil, true)
}

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

func (uc *Interactor) MarkRead(id int64) error {
	_, err := uc.repo.Patch(id, map[string]interface{}{"read": true})
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
