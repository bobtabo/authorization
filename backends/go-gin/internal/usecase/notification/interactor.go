// Package notification は通知ユースケースを提供します。
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

// NewInteractor は Interactor を生成します。
//
// repo: 通知リポジトリ
// staffRepo: スタッフリポジトリ
func NewInteractor(repo domnotification.Repository, staffRepo domstaff.Repository) *Interactor {
	return &Interactor{repo: repo, staffRepo: staffRepo}
}

// ListPage はカーソルページングで通知一覧の値オブジェクトを返します。
//
// staffID: スタッフID
// cursor: ページカーソル（nil で先頭から）
// limit: 取得件数上限（1〜100 にクランプ）
// 戻り値: 通知ページ Vo、またはエラー
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

// Counts はスタッフの未読・全体通知数を返します。
//
// staffID: スタッフID
// 戻り値: 未読数、全件数、またはエラー
func (uc *Interactor) Counts(staffID uint) (unread, total int64, err error) {
	return uc.repo.Counts(staffID)
}

// BulkMarkRead はスタッフの全通知を既読にして更新件数を返します。
//
// staffID: スタッフID
// 戻り値: 更新件数、またはエラー
func (uc *Interactor) BulkMarkRead(staffID uint) (int64, error) {
	return uc.repo.BulkMarkRead(int64(staffID), nil, true)
}

// FanOut は全アクティブスタッフへ通知を配信します。
//
// dto: 通知配信 Dto
// 戻り値: エラー
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

// MarkRead は通知を既読にします。
//
// id: 通知ID
// 戻り値: エラー
func (uc *Interactor) MarkRead(id int64) error {
	_, err := uc.repo.Patch(id, map[string]interface{}{"read": true})
	return err
}

// ---------- 変換ヘルパー ----------

// notificationToItem は通知エンティティを一覧用 Vo に変換します。
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
