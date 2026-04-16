package staff

import (
	domstaff "authorization-go/internal/domain/staff"
	"authorization-go/pkg/apperror"
)

// Interactor はスタッフのユースケースを実装します。
type Interactor struct {
	repo domstaff.Repository
}

func NewInteractor(repo domstaff.Repository) *Interactor {
	return &Interactor{repo: repo}
}

// FindByCondition は検索条件に合致するスタッフ一覧を返します。
func (uc *Interactor) FindByCondition(cond domstaff.Condition) ([]*domstaff.Staff, error) {
	return uc.repo.FindByCondition(cond)
}

// UpdateRole はスタッフの権限を更新します。
func (uc *Interactor) UpdateRole(dto UpdateRoleDto) error {
	if dto.Role != domstaff.RoleAdmin && dto.Role != domstaff.RoleMember {
		return apperror.BadRequest("role_invalid")
	}
	ok, err := uc.repo.UpdateRole(dto.ID, dto.Role, dto.ExecutorID)
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
	ok, err := uc.repo.SoftDelete(dto.ID, dto.ExecutorID)
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// Restore はスタッフの論理削除を復元します。
func (uc *Interactor) Restore(id uint) error {
	ok, err := uc.repo.Restore(id)
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// Status はスタッフの表示用ステータスを返します（削除済み=0, 有効=1）。
func Status(s *domstaff.Staff) int {
	if s.DeletedAt != nil {
		return 0
	}
	return 1
}
