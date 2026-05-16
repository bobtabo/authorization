package invitation

import (
	dominvitation "authorization-go-beego/internal/domain/invitation"
	"authorization-go-beego/pkg/apperror"
)

type Interactor struct {
	repo     dominvitation.Repository
	authRepo dominvitation.AuthRepository
}

func NewInteractor(repo dominvitation.Repository, authRepo dominvitation.AuthRepository) *Interactor {
	return &Interactor{repo: repo, authRepo: authRepo}
}

func (uc *Interactor) Current(role int) (*dominvitation.Vo, error) {
	result, err := uc.repo.GetCurrentByRole(role)
	if err != nil {
		return nil, err
	}
	if result == nil {
		return nil, apperror.NotFound("invitation_not_found")
	}
	return result, nil
}

func (uc *Interactor) Issue(role int) (*dominvitation.Vo, error) {
	return uc.repo.Issue(role)
}

func (uc *Interactor) FindByToken(dto FindByTokenDto) (*dominvitation.Vo, error) {
	if dto.Token == "" {
		return nil, apperror.BadRequest("invitation_invalid")
	}
	result, err := uc.repo.FindByToken(dto.Token)
	if err != nil {
		return nil, err
	}
	if result == nil {
		return nil, apperror.BadRequest("invitation_invalid")
	}
	if err := uc.authRepo.Store(result.Token, result.Role, 600); err != nil {
		return nil, err
	}
	return result, nil
}
