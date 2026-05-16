package persistence

import (
	dominvitation "authorization-go/internal/domain/invitation"
	"authorization-go/internal/infrastructure/model"
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"strings"
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

// GetCurrentByRole はロールで絞り込んだ最新の招待情報の値オブジェクトを返します。
func (r *GormInvitationRepository) GetCurrentByRole(role int) (*dominvitation.Vo, error) {
	var m model.Invitation
	if err := r.db.Where("role = ?", role).Order("id DESC").First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return r.buildVo(m.Token, m.Role), nil
}

// Issue は新しい招待トークンを生成して保存し、値オブジェクトを返します。
func (r *GormInvitationRepository) Issue(role int) (*dominvitation.Vo, error) {
	token, err := generateInvitationToken()
	if err != nil {
		return nil, err
	}
	now  := time.Now()
	zero := uint(0)
	m := model.Invitation{
		Token:     token,
		Role:      role,
		CreatedAt: now,
		CreatedBy: &zero,
		UpdatedAt: now,
		UpdatedBy: &zero,
	}
	if err = r.db.Create(&m).Error; err != nil {
		return nil, err
	}
	return r.buildVo(token, role), nil
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
	return r.buildVo(m.Token, m.Role), nil
}

func (r *GormInvitationRepository) buildVo(token string, role int) *dominvitation.Vo {
	url := fmt.Sprintf("%s/invitation/%s", r.frontendURL, token)
	return &dominvitation.Vo{Token: token, URL: url, DisplayURL: buildDisplayURL(url), Role: role}
}

// buildDisplayURL は PHP の buildDisplayUrl と同じロジックで表示用 URL を生成します。
// /invitation/ より手前はそのまま保持し、トークン部分だけを head+...+tail に省略します。
func buildDisplayURL(url string) string {
	const segment = "/invitation/"
	idx := strings.Index(url, segment)
	if idx == -1 {
		if len(url) > 72 {
			return url[:68] + "..."
		}
		return url
	}
	base := url[:idx+len(segment)]
	after := url[idx+len(segment):]
	tokenEnd := strings.IndexAny(after, "?#")
	if tokenEnd == -1 {
		tokenEnd = len(after)
	}
	tok := after[:tokenEnd]
	suffix := after[tokenEnd:]
	const head, tail = 6, 4
	if len(tok) <= head+tail+3 {
		return url
	}
	return base + tok[:head] + "..." + tok[len(tok)-tail:] + suffix
}

func generateInvitationToken() (string, error) {
	b := make([]byte, 16)
	if _, err := rand.Read(b); err != nil {
		return "", err
	}
	return hex.EncodeToString(b), nil
}
