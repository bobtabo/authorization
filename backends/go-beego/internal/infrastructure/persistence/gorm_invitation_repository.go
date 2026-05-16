package persistence

import (
	dominvitation "authorization-go-beego/internal/domain/invitation"
	"authorization-go-beego/internal/infrastructure/model"
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"strings"
	"time"

	"github.com/beego/beego/v2/client/orm"
)

type OrmInvitationRepository struct {
	o           QueryOrmer
	frontendURL string
}

func NewOrmInvitationRepository(o QueryOrmer, frontendURL string) *OrmInvitationRepository {
	return &OrmInvitationRepository{o: o, frontendURL: frontendURL}
}

func (r *OrmInvitationRepository) GetCurrentByRole(role int) (*dominvitation.Vo, error) {
	var m model.Invitation
	err := r.o.QueryTable(new(model.Invitation)).
		Filter("deleted_at__isnull", true).
		Filter("role", role).
		OrderBy("-id").
		One(&m)
	if err == orm.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return r.buildVo(m.Token, m.Role), nil
}

func (r *OrmInvitationRepository) Issue(role int) (*dominvitation.Vo, error) {
	token, err := generateInvitationToken()
	if err != nil {
		return nil, err
	}
	now := time.Now()
	zero := uint(0)
	m := model.Invitation{
		Token:     token,
		Role:      role,
		CreatedAt: now,
		CreatedBy: &zero,
		UpdatedAt: now,
		UpdatedBy: &zero,
	}
	if _, err = r.o.Insert(&m); err != nil {
		return nil, err
	}
	return r.buildVo(token, role), nil
}

func (r *OrmInvitationRepository) FindByToken(token string) (*dominvitation.Vo, error) {
	var m model.Invitation
	err := r.o.QueryTable(new(model.Invitation)).
		Filter("token", token).
		Filter("deleted_at__isnull", true).
		One(&m)
	if err == orm.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return r.buildVo(m.Token, m.Role), nil
}

func (r *OrmInvitationRepository) buildVo(token string, role int) *dominvitation.Vo {
	url := fmt.Sprintf("%s/invitation/%s", r.frontendURL, token)
	return &dominvitation.Vo{Token: token, Role: role, URL: url, DisplayURL: buildDisplayURL(url)}
}

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
