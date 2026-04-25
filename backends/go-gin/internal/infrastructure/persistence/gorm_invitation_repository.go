package persistence

import (
	dominvitation "authorization-go/internal/domain/invitation"
	"authorization-go/internal/infrastructure/model"
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"time"

	"gorm.io/gorm"
)

// GormInvitationRepository は domain/invitation.Repository の GORM 実装です。
type GormInvitationRepository struct {
	db          *gorm.DB
	frontendURL string
}

// NewGormInvitationRepository は GormInvitationRepository を生成します。
//
// db: GORM DB インスタンス
// frontendURL: フロントエンドのベース URL（招待 URL 生成に使用）
func NewGormInvitationRepository(db *gorm.DB, frontendURL string) *GormInvitationRepository {
	return &GormInvitationRepository{db: db, frontendURL: frontendURL}
}

// GetCurrent は最新の招待情報の値オブジェクトを返します。
func (r *GormInvitationRepository) GetCurrent() (*dominvitation.Vo, error) {
	var m model.Invitation
	if err := r.db.Order("id DESC").First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return r.buildVo(m.Token), nil
}

// Issue は新しい招待トークンを生成して保存し、値オブジェクトを返します。
func (r *GormInvitationRepository) Issue() (*dominvitation.Vo, error) {
	token, err := generateInvitationToken()
	if err != nil {
		return nil, err
	}
	now := time.Now()
	m := model.Invitation{
		Token:     token,
		CreatedAt: now,
		UpdatedAt: now,
	}
	if err = r.db.Create(&m).Error; err != nil {
		return nil, err
	}
	return r.buildVo(token), nil
}

// FindByToken はトークンで招待情報の値オブジェクトを返します。
func (r *GormInvitationRepository) FindByToken(token string) (*dominvitation.Vo, error) {
	var m model.Invitation
	if err := r.db.Where("token = ?", token).First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return r.buildVo(m.Token), nil
}

func (r *GormInvitationRepository) buildVo(token string) *dominvitation.Vo {
	url := fmt.Sprintf("%s/register?token=%s", r.frontendURL, token)
	displayURL := url
	if len(displayURL) > 50 {
		displayURL = displayURL[:20] + "..." + displayURL[len(displayURL)-20:]
	}
	return &dominvitation.Vo{Token: token, URL: url, DisplayURL: displayURL}
}

func generateInvitationToken() (string, error) {
	b := make([]byte, 16)
	if _, err := rand.Read(b); err != nil {
		return "", err
	}
	return hex.EncodeToString(b), nil
}
