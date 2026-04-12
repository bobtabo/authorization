package service

import (
	"authorization-go/internal/model"
	"authorization-go/internal/repository"
	"authorization-go/pkg/apperror"
	"time"
)

type AuthService struct {
	staffRepo *repository.StaffRepository
}

func NewAuthService(staffRepo *repository.StaffRepository) *AuthService {
	return &AuthService{staffRepo: staffRepo}
}

// FindUser はIDでスタッフを取得します。
func (s *AuthService) FindUser(id uint) (*model.Staff, error) {
	staff, err := s.staffRepo.FindByID(id)
	if err != nil {
		return nil, err
	}
	if staff == nil {
		return nil, apperror.NotFound("user_not_found")
	}
	return staff, nil
}

// Login はソーシャル認証でログインします（未登録の場合は新規作成）。
func (s *AuthService) Login(provider int, providerID, name, email string, avatar *string) (*model.Staff, error) {
	existing, err := s.staffRepo.FindByProvider(provider, providerID)
	if err != nil {
		return nil, err
	}

	now := time.Now()
	if existing == nil {
		newStaff := &model.Staff{
			Name:        name,
			Email:       email,
			Provider:    provider,
			ProviderID:  providerID,
			Avatar:      avatar,
			Role:        2, // Member
			LastLoginAt: &now,
			CreatedAt:   now,
			UpdatedAt:   now,
		}
		return s.staffRepo.Save(newStaff)
	}

	existing.Avatar = avatar
	existing.LastLoginAt = &now
	existing.UpdatedAt = now
	return s.staffRepo.Save(existing)
}
