// Package staff はスタッフユースケースを提供します。
package staff

import (
	domstaff "authorization-go/internal/domain/staff"
	"authorization-go/pkg/apperror"
)

// Interactor はスタッフのユースケースを実装します。
type Interactor struct {
	repo domstaff.Repository
}

// NewInteractor は Interactor を生成します。
//
// repo: スタッフリポジトリ
func NewInteractor(repo domstaff.Repository) *Interactor {
	return &Interactor{repo: repo}
}

// FindByCondition は検索条件に合致するスタッフ一覧の値オブジェクトを返します。
//
// cond: 検索条件
// 戻り値: スタッフ一覧 Vo のスライス、またはエラー
func (uc *Interactor) FindByCondition(cond domstaff.Condition) ([]*domstaff.ListItem, error) {
	staffs, err := uc.repo.FindByCondition(cond)
	if err != nil {
		return nil, err
	}
	items := make([]*domstaff.ListItem, 0, len(staffs))
	for _, s := range staffs {
		items = append(items, staffToListItem(s))
	}
	return items, nil
}

// UpdateRole はスタッフの権限を更新します。
//
// dto: ロール更新 Dto
// 戻り値: エラー
func (uc *Interactor) UpdateRole(dto UpdateRoleDto) error {
	if dto.Role != domstaff.RoleAdmin && dto.Role != domstaff.RoleMember {
		return apperror.BadRequest("role_invalid")
	}
	s, err := uc.repo.FindByID(dto.ID)
	if err != nil {
		return err
	}
	if s == nil || s.DeletedAt != nil {
		return apperror.NotFound("staff_not_found")
	}
	ok, err := uc.repo.UpdateRole(dto.ID, dto.Role, dto.ExecutorID, s.Version)
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// Destroy はスタッフを論理削除します。
//
// dto: 論理削除 Dto
// 戻り値: エラー
func (uc *Interactor) Destroy(dto DestroyDto) error {
	s, err := uc.repo.FindByID(dto.ID)
	if err != nil {
		return err
	}
	if s == nil || s.DeletedAt != nil {
		return apperror.NotFound("staff_not_found")
	}
	ok, err := uc.repo.SoftDelete(dto.ID, dto.ExecutorID, s.Version)
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// Restore はスタッフの論理削除を復元します。
//
// id: スタッフID
// 戻り値: エラー
func (uc *Interactor) Restore(id uint) error {
	s, err := uc.repo.FindByIDUnscoped(id)
	if err != nil {
		return err
	}
	if s == nil || s.DeletedAt == nil {
		return apperror.NotFound("staff_not_found")
	}
	ok, err := uc.repo.Restore(id, s.Version)
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// ---------- 変換ヘルパー ----------

// staffToListItem はスタッフエンティティを一覧用 Vo に変換します。
func staffToListItem(s *domstaff.Staff) *domstaff.ListItem {
	return &domstaff.ListItem{
		ID:        s.ID,
		Name:      s.Name,
		Email:     s.Email,
		Role:      s.Role,
		Status:    staffStatus(s),
		CreatedAt: s.CreatedAt,
		UpdatedAt: s.UpdatedAt,
	}
}

// staffStatus はスタッフの表示用ステータスを返します（削除済み=0, 有効=1）。
func staffStatus(s *domstaff.Staff) int {
	if s.DeletedAt != nil {
		return 0
	}
	return 1
}
