// Package staff はスタッフユースケースを提供します。
package staff

import (
	domstaff "authorization-go-beego/internal/domain/staff"
	"authorization-go-beego/pkg/apperror"
)

// Interactor はスタッフユースケースの実装です。
type Interactor struct {
	repo domstaff.Repository
}

// NewInteractor は Interactor を生成します。
func NewInteractor(repo domstaff.Repository) *Interactor {
	return &Interactor{repo: repo}
}

// CountByCondition は条件に合うスタッフの総件数を返します。
func (uc *Interactor) CountByCondition(cond domstaff.Condition) (int, error) {
	return uc.repo.CountByCondition(cond)
}

// FindByCondition は条件に合うスタッフ一覧を取得します。
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

// UpdateRole はスタッフのロールを変更します。
func (uc *Interactor) UpdateRole(dto UpdateRoleDto) error {
	if dto.Role != domstaff.RoleAdmin && dto.Role != domstaff.RoleMember {
		return apperror.BadRequest("role_invalid")
	}
	s, err := uc.repo.FindByID(&domstaff.Staff{ID: dto.ID})
	if err != nil {
		return err
	}
	if s == nil {
		return apperror.NotFound("staff_not_found")
	}
	ok, err := uc.repo.UpdateRole(&domstaff.Staff{ID: dto.ID, Role: dto.Role, UpdatedBy: &dto.ExecutorID, Version: s.Version})
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// Destroy はスタッフを論理削除します。
func (uc *Interactor) Destroy(dto DestroyDto) error {
	s, err := uc.repo.FindByID(&domstaff.Staff{ID: dto.ID})
	if err != nil {
		return err
	}
	if s == nil {
		return apperror.NotFound("staff_not_found")
	}
	ok, err := uc.repo.SoftDelete(&domstaff.Staff{ID: dto.ID, DeletedBy: &dto.ExecutorID, Version: s.Version})
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// Restore は論理削除したスタッフを復元します。
func (uc *Interactor) Restore(dto RestoreDto) error {
	ok, err := uc.repo.Restore(&domstaff.Staff{ID: dto.ID})
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// staffToListItem はスタッフエンティティを一覧用VOに変換します。
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

// staffStatus はスタッフの有効/削除状態を返します。
func staffStatus(s *domstaff.Staff) int {
	if s.DeletedAt != nil {
		return 0
	}
	return 1
}
