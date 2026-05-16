// Package auth は認証ユースケースを提供します。
package auth

import (
	dominvitation "authorization-go-echo/internal/domain/invitation"
	domstaff "authorization-go-echo/internal/domain/staff"
	"authorization-go-echo/pkg/apperror"
	"time"
)

// Interactor は認証ユースケースの実装です。
type Interactor struct {
	staffRepo          domstaff.Repository
	invitationAuthRepo dominvitation.AuthRepository
}

// NewInteractor は Interactor を生成します。
func NewInteractor(staffRepo domstaff.Repository, invitationAuthRepo dominvitation.AuthRepository) *Interactor {
	return &Interactor{staffRepo: staffRepo, invitationAuthRepo: invitationAuthRepo}
}

// FindUser はIDでスタッフプロフィールを取得します。
func (uc *Interactor) FindUser(dto FindUserDto) (*domstaff.Vo, error) {
	s, err := uc.staffRepo.FindByID(&domstaff.Staff{ID: dto.ID})
	if err != nil {
		return nil, err
	}
	if s == nil {
		return nil, apperror.NotFound("user_not_found")
	}
	return staffToVo(s), nil
}

// Login はSSOコールバック情報を元にスタッフを登録またはログインします。
func (uc *Interactor) Login(dto LoginDto) (*domstaff.Vo, error) {
	existing, err := uc.staffRepo.FindByProvider(&domstaff.Staff{Provider: dto.Provider, ProviderID: dto.ProviderID})
	if err != nil {
		return nil, err
	}

	now := time.Now()
	if existing == nil {
		if dto.InvitationToken == "" {
			return nil, apperror.Forbidden("invitation_required")
		}
		rolePtr, err := uc.invitationAuthRepo.Find(dto.InvitationToken)
		if err != nil {
			return nil, err
		}
		if rolePtr == nil {
			return nil, apperror.Forbidden("invitation_required")
		}
		if err := uc.invitationAuthRepo.Remove(dto.InvitationToken); err != nil {
			return nil, err
		}

		zero := uint(0)
		newStaff := &domstaff.Staff{
			Name:        dto.Name,
			Email:       dto.Email,
			Provider:    dto.Provider,
			ProviderID:  dto.ProviderID,
			Avatar:      dto.Avatar,
			Role:        *rolePtr,
			LastLoginAt: &now,
			CreatedAt:   now,
			CreatedBy:   &zero,
			UpdatedAt:   now,
			UpdatedBy:   &zero,
		}
		saved, err := uc.staffRepo.Save(newStaff)
		if err != nil {
			return nil, err
		}
		return staffToVo(saved), nil
	}

	existing.Avatar = dto.Avatar
	existing.LastLoginAt = &now
	existing.UpdatedAt = now
	saved, err := uc.staffRepo.Save(existing)
	if err != nil {
		return nil, err
	}
	return staffToVo(saved), nil
}

func staffToVo(s *domstaff.Staff) *domstaff.Vo {
	return &domstaff.Vo{
		ID:     s.ID,
		Name:   s.Name,
		Avatar: s.Avatar,
		Role:   s.Role,
	}
}
