package service

import (
	"authorization-go/internal/model"
	"authorization-go/internal/repository"
	"authorization-go/pkg/apperror"
)

type StaffService struct {
	repo *repository.StaffRepository
}

func NewStaffService(repo *repository.StaffRepository) *StaffService {
	return &StaffService{repo: repo}
}

func (s *StaffService) FindByCondition(f repository.StaffFilter) ([]*model.Staff, error) {
	return s.repo.FindByCondition(f)
}

// UpdateRole はスタッフの権限を更新します。
func (s *StaffService) UpdateRole(id uint, role int, updatedBy uint) error {
	if role != 1 && role != 2 {
		return apperror.BadRequest("role_invalid")
	}
	ok, err := s.repo.UpdateRole(id, role, updatedBy)
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// Destroy はスタッフを論理削除します。
func (s *StaffService) Destroy(id uint, deletedBy uint) error {
	ok, err := s.repo.SoftDelete(id, deletedBy)
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// Restore はスタッフの論理削除を復元します。
func (s *StaffService) Restore(id uint) error {
	ok, err := s.repo.Restore(id)
	if err != nil {
		return err
	}
	if !ok {
		return apperror.NotFound("staff_not_found")
	}
	return nil
}

// StaffStatus はスタッフの表示用ステータスを返します（削除済み=0, 有効=1）。
func StaffStatus(s *model.Staff) int {
	if s.DeletedAt.Valid {
		return 0
	}
	return 1
}
