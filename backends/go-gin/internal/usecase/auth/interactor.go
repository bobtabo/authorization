// Package auth は認証ユースケースを提供します。
package auth

import (
	domstaff "authorization-go/internal/domain/staff"
	"authorization-go/pkg/apperror"
	"time"
)

// Interactor は認証のユースケースを実装します。
type Interactor struct {
	repo domstaff.Repository
}

// NewInteractor は Interactor を生成します。
//
// repo: スタッフリポジトリ
func NewInteractor(repo domstaff.Repository) *Interactor {
	return &Interactor{repo: repo}
}

// FindUser はIDでスタッフを取得し、レスポンス用 Vo を返します。
//
// id: スタッフID
// 戻り値: スタッフ Vo、またはエラー
func (uc *Interactor) FindUser(id uint) (*domstaff.Vo, error) {
	s, err := uc.repo.FindByID(id)
	if err != nil {
		return nil, err
	}
	if s == nil {
		return nil, apperror.NotFound("user_not_found")
	}
	return staffToVo(s), nil
}

// Login はソーシャル認証でログインし、レスポンス用 Vo を返します。
// 未登録の場合は新規スタッフを作成します。
//
// dto: ログイン情報 Dto
// 戻り値: スタッフ Vo、またはエラー
func (uc *Interactor) Login(dto LoginDto) (*domstaff.Vo, error) {
	existing, err := uc.repo.FindByProvider(dto.Provider, dto.ProviderID)
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
		saved, err := uc.repo.Save(newStaff)
		if err != nil {
			return nil, err
		}
		return staffToVo(saved), nil
	}

	existing.Avatar = dto.Avatar
	existing.LastLoginAt = &now
	existing.UpdatedAt = now
	saved, err := uc.repo.Save(existing)
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
