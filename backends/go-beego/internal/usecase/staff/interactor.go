package staff

import (
	domstaff "authorization-go-beego/internal/domain/staff"
	"authorization-go-beego/pkg/apperror"
)

type Interactor struct {
	repo domstaff.Repository
}

func NewInteractor(repo domstaff.Repository) *Interactor {
	return &Interactor{repo: repo}
}

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

func staffStatus(s *domstaff.Staff) int {
	if s.DeletedAt != nil {
		return 0
	}
	return 1
}
