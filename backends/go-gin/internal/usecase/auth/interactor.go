// Package auth は認証ユースケースを提供します。
package auth

import (
	dominvitation "authorization-go/internal/domain/invitation"
	domstaff "authorization-go/internal/domain/staff"
	"authorization-go/pkg/apperror"
	"time"
)

// Interactor は認証のユースケースを実装します。
type Interactor struct {
	staffRepo          domstaff.Repository
	invitationAuthRepo dominvitation.AuthRepository
}

// NewInteractor は Interactor を生成します。
//
// staffRepo: スタッフリポジトリ
// invitationAuthRepo: 招待認証キャッシュリポジトリ
func NewInteractor(staffRepo domstaff.Repository, invitationAuthRepo dominvitation.AuthRepository) *Interactor {
	return &Interactor{staffRepo: staffRepo, invitationAuthRepo: invitationAuthRepo}
}

// FindUser はIDでスタッフを取得し、レスポンス用 Vo を返します。
//
// id: スタッフID
// 戻り値: スタッフ Vo、またはエラー
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

// Login はソーシャル認証でログインし、レスポンス用 Vo を返します。
// 未登録の場合は招待トークンを検証してから新規スタッフを作成します。
//
// dto: ログイン情報 Dto
// 戻り値: スタッフ Vo、またはエラー
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

// staffToVo はスタッフエンティティをレスポンス用 Vo に変換します。
func staffToVo(s *domstaff.Staff) *domstaff.Vo {
	return &domstaff.Vo{
		ID:     s.ID,
		Name:   s.Name,
		Avatar: s.Avatar,
		Role:   s.Role,
	}
}
