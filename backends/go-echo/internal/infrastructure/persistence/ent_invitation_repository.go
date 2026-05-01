package persistence

import (
	"authorization-go-echo/ent"
	"authorization-go-echo/ent/invitation"
	dominvitation "authorization-go-echo/internal/domain/invitation"
	"context"
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"strings"
	"time"
)

type EntInvitationRepository struct {
	db          *ent.Client
	frontendURL string
}

func NewEntInvitationRepository(db *ent.Client, frontendURL string) *EntInvitationRepository {
	return &EntInvitationRepository{db: db, frontendURL: frontendURL}
}

func (r *EntInvitationRepository) GetCurrent() (*dominvitation.Vo, error) {
	m, err := r.db.Invitation.Query().
		Order(ent.Desc(invitation.FieldID)).
		First(context.Background())
	if err != nil {
		if ent.IsNotFound(err) {
			return nil, nil
		}
		return nil, err
	}
	return r.buildVo(m.Token), nil
}

func (r *EntInvitationRepository) Issue() (*dominvitation.Vo, error) {
	token, err := generateEntInvitationToken()
	if err != nil {
		return nil, err
	}
	now := time.Now()
	zero := uint(0)
	_, err = r.db.Invitation.Create().
		SetToken(token).
		SetCreatedAt(now).
		SetCreatedBy(zero).
		SetUpdatedAt(now).
		SetUpdatedBy(zero).
		Save(context.Background())
	if err != nil {
		return nil, err
	}
	return r.buildVo(token), nil
}

func (r *EntInvitationRepository) FindByToken(token string) (*dominvitation.Vo, error) {
	m, err := r.db.Invitation.Query().
		Where(invitation.TokenEQ(token), invitation.DeletedAtIsNil()).
		First(context.Background())
	if err != nil {
		if ent.IsNotFound(err) {
			return nil, nil
		}
		return nil, err
	}
	return r.buildVo(m.Token), nil
}

func (r *EntInvitationRepository) buildVo(token string) *dominvitation.Vo {
	url := fmt.Sprintf("%s/invitation/%s", r.frontendURL, token)
	return &dominvitation.Vo{Token: token, URL: url, DisplayURL: buildEntDisplayURL(url)}
}

func buildEntDisplayURL(url string) string {
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

func generateEntInvitationToken() (string, error) {
	b := make([]byte, 16)
	if _, err := rand.Read(b); err != nil {
		return "", err
	}
	return hex.EncodeToString(b), nil
}
