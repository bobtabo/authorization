package invitation

import (
	dominvitation "authorization-go/internal/domain/invitation"
	"authorization-go/pkg/apperror"
)

// Interactor は招待のユースケースを実装します。
type Interactor struct {
	repo dominvitation.Repository
}

func NewInteractor(repo dominvitation.Repository) *Interactor {
	return &Interactor{repo: repo}
}

// Current は最新の招待情報を取得します。
func (uc *Interactor) Current() (*dominvitation.Vo, error) {
	result, err := uc.repo.GetCurrent()
	if err != nil {
		return nil, err
	}
	if result == nil {
		return nil, apperror.NotFound("invitation_not_found")
	}
	return result, nil
}

// Issue は新しい招待を発行します。
func (uc *Interactor) Issue() (*dominvitation.Vo, error) {
	return uc.repo.Issue()
}

// FindByToken はトークンで招待を検索します。
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
	return result, nil
}
