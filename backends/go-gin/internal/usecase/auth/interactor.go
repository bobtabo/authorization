package auth

import (
	domstaff "authorization-go/internal/domain/staff"
	"authorization-go/pkg/apperror"
	"time"
)

// Interactor は認証のユースケースを実装します。
type Interactor struct {
	staffRepo domstaff.Repository
}

func NewInteractor(staffRepo domstaff.Repository) *Interactor {
	return &Interactor{staffRepo: staffRepo}
}

// FindUser はIDでスタッフを取得します。
func (uc *Interactor) FindUser(id uint) (*domstaff.Staff, error) {
	s, err := uc.staffRepo.FindByID(id)
	if err != nil {
		return nil, err
	}
	if s == nil {
		return nil, apperror.NotFound("user_not_found")
	}
	return s, nil
}

// Login はソーシャル認証でログインします（未登録の場合は新規作成）。
func (uc *Interactor) Login(dto LoginDto) (*domstaff.Staff, error) {
	existing, err := uc.staffRepo.FindByProvider(dto.Provider, dto.ProviderID)
	if err != nil {
		return nil, err
	}

	now := time.Now()
	if existing == nil {
		newStaff := &domstaff.Staff{
			Name:        dto.Name,
			Email:       dto.Email,
			Provider:    dto.Provider,
			ProviderID:  dto.ProviderID,
			Avatar:      dto.Avatar,
			Role:        domstaff.RoleMember,
			LastLoginAt: &now,
			CreatedAt:   now,
			UpdatedAt:   now,
		}
		return uc.staffRepo.Save(newStaff)
	}

	existing.Avatar = dto.Avatar
	existing.LastLoginAt = &now
	existing.UpdatedAt = now
	return uc.staffRepo.Save(existing)
}
