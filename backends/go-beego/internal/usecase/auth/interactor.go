package auth

import (
	dominvitation "authorization-go-beego/internal/domain/invitation"
	domstaff "authorization-go-beego/internal/domain/staff"
	"authorization-go-beego/pkg/apperror"
	"time"
)

type Interactor struct {
	staffRepo          domstaff.Repository
	invitationAuthRepo dominvitation.AuthRepository
}

func NewInteractor(staffRepo domstaff.Repository, invitationAuthRepo dominvitation.AuthRepository) *Interactor {
	return &Interactor{staffRepo: staffRepo, invitationAuthRepo: invitationAuthRepo}
}

func (uc *Interactor) FindUser(id uint) (*domstaff.Vo, error) {
	s, err := uc.staffRepo.FindByID(id)
	if err != nil {
		return nil, err
	}
	if s == nil {
		return nil, apperror.NotFound("user_not_found")
	}
	return staffToVo(s), nil
}

func (uc *Interactor) Login(dto LoginDto) (*domstaff.Vo, error) {
	existing, err := uc.staffRepo.FindByProvider(dto.Provider, dto.ProviderID)
	if err != nil {
		return nil, err
	}

	now := time.Now()
	if existing == nil {
		if dto.InvitationToken == "" {
			return nil, apperror.Forbidden("invitation_required")
		}
		found, err := uc.invitationAuthRepo.Find(dto.InvitationToken)
		if err != nil {
			return nil, err
		}
		if found == "" {
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
			Role:        domstaff.RoleMember,
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
