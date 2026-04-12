package repository

import (
	"authorization-go/internal/model"
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"time"

	"gorm.io/gorm"
)

type InvitationRepository struct {
	db          *gorm.DB
	frontendURL string
}

func NewInvitationRepository(db *gorm.DB, frontendURL string) *InvitationRepository {
	return &InvitationRepository{db: db, frontendURL: frontendURL}
}

type InvitationResult struct {
	Token      string
	URL        string
	DisplayURL string
}

func (r *InvitationRepository) GetCurrent() (*InvitationResult, error) {
	var inv model.Invitation
	if err := r.db.Order("id DESC").First(&inv).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return r.buildResult(inv.Token), nil
}

func (r *InvitationRepository) Issue() (*InvitationResult, error) {
	token, err := generateToken()
	if err != nil {
		return nil, err
	}

	now := time.Now()
	inv := model.Invitation{
		Token:     token,
		CreatedAt: now,
		UpdatedAt: now,
	}
	if err := r.db.Create(&inv).Error; err != nil {
		return nil, err
	}
	return r.buildResult(token), nil
}

func (r *InvitationRepository) FindByToken(token string) (*InvitationResult, error) {
	var inv model.Invitation
	if err := r.db.Where("token = ?", token).First(&inv).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return r.buildResult(inv.Token), nil
}

func (r *InvitationRepository) buildResult(token string) *InvitationResult {
	url := fmt.Sprintf("%s/register?token=%s", r.frontendURL, token)
	displayURL := url
	if len(displayURL) > 50 {
		displayURL = displayURL[:20] + "..." + displayURL[len(displayURL)-20:]
	}
	return &InvitationResult{Token: token, URL: url, DisplayURL: displayURL}
}

func generateToken() (string, error) {
	b := make([]byte, 16)
	if _, err := rand.Read(b); err != nil {
		return "", err
	}
	return hex.EncodeToString(b), nil
}
