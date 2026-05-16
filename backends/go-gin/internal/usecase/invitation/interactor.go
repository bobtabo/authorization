// Package invitation は招待ユースケースを提供します。
package invitation

import (
	dominvitation "authorization-go/internal/domain/invitation"
	"authorization-go/pkg/apperror"
)

// Interactor は招待のユースケースを実装します。
type Interactor struct {
	repo     dominvitation.Repository
	authRepo dominvitation.AuthRepository
}

// NewInteractor は Interactor を生成します。
//
// repo: 招待リポジトリ
// authRepo: 招待認証キャッシュリポジトリ
func NewInteractor(repo dominvitation.Repository, authRepo dominvitation.AuthRepository) *Interactor {
	return &Interactor{repo: repo, authRepo: authRepo}
}

// Current は指定ロールの最新の招待情報の値オブジェクトを返します。
//
// dto: ロール指定 Dto
// 戻り値: 招待 Vo、またはエラー
func (uc *Interactor) Current(dto CurrentDto) (*dominvitation.Vo, error) {
	result, err := uc.repo.GetCurrentByRole(dto.Role)
	if err != nil {
		return nil, err
	}
	if result == nil {
		return nil, apperror.NotFound("invitation_not_found")
	}
	return result, nil
}

// Issue は新しい招待トークンを発行し、招待情報の値オブジェクトを返します。
//
// dto: ロール指定 Dto
// 戻り値: 招待 Vo、またはエラー
func (uc *Interactor) Issue(dto IssueDto) (*dominvitation.Vo, error) {
	return uc.repo.Issue(dto.Role)
}

// FindByToken はトークンで招待情報を返し、招待認証ロールをキャッシュします。
//
// dto: トークン検索 Dto
// 戻り値: 招待 Vo、またはエラー
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
