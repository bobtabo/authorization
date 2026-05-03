package persistence

import (
	domclient "authorization-go-beego/internal/domain/client"
	"authorization-go-beego/internal/infrastructure/model"
	"authorization-go-beego/pkg/apperror"
	"time"

	"github.com/beego/beego/v2/client/orm"
)

type OrmClientRepository struct {
	o QueryOrmer
}

func NewOrmClientRepository(o QueryOrmer) *OrmClientRepository {
	return &OrmClientRepository{o: o}
}

func (r *OrmClientRepository) FindByCondition(cond domclient.Condition) ([]*domclient.Client, error) {
	qs := r.o.QueryTable(new(model.Client)).OrderBy("id")
	if cond.Keyword != nil && *cond.Keyword != "" {
		qs = qs.Filter("name__contains", *cond.Keyword)
	}
	if cond.StartFrom != nil {
		qs = qs.Filter("start_at__gte", cond.StartFrom)
	}
	if cond.StartTo != nil {
		qs = qs.Filter("start_at__lte", cond.StartTo)
	}
	if len(cond.Statuses) > 0 {
		qs = qs.Filter("status__in", cond.Statuses)
	}
	var ms []*model.Client
	if _, err := qs.All(&ms); err != nil {
		return nil, err
	}
	out := make([]*domclient.Client, 0, len(ms))
	for _, m := range ms {
		out = append(out, clientToDomain(m))
	}
	return out, nil
}

func (r *OrmClientRepository) FindByID(c *domclient.Client) (*domclient.Client, error) {
	var m model.Client
	err := r.o.QueryTable(new(model.Client)).Filter("id", c.ID).One(&m)
	if err == orm.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return clientToDomain(&m), nil
}

func (r *OrmClientRepository) FindByAccessToken(c *domclient.Client) (*domclient.Client, error) {
	var m model.Client
	err := r.o.QueryTable(new(model.Client)).
		Filter("access_token", c.AccessToken).
		Filter("status", domclient.StatusActive).
		Filter("deleted_at__isnull", true).
		One(&m)
	if err == orm.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return clientToDomain(&m), nil
}

func (r *OrmClientRepository) FindByIdentifier(c *domclient.Client) (*domclient.Client, error) {
	var m model.Client
	err := r.o.QueryTable(new(model.Client)).
		Filter("identifier", c.Identifier).
		Filter("deleted_at__isnull", true).
		One(&m)
	if err == orm.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return clientToDomain(&m), nil
}

func (r *OrmClientRepository) Save(c *domclient.Client) (*domclient.Client, error) {
	m := clientToModel(c)
	if m.ID == 0 {
		if _, err := r.o.Insert(m); err != nil {
			return nil, err
		}
	} else {
		res, err := r.o.Raw(
			`UPDATE clients SET
				name=?, identifier=?, post_code=?, pref=?, city=?, address=?, building=?,
				tel=?, email=?, access_token=?, private_key=?, public_key=?, fingerprint=?,
				status=?, start_at=?, stop_at=?,
				created_at=?, created_by=?, updated_at=?, updated_by=?,
				deleted_at=?, deleted_by=?, version=version+1
			WHERE id=? AND version=?`,
			m.Name, m.Identifier, m.PostCode, m.Pref, m.City, m.Address, m.Building,
			m.Tel, m.Email, m.AccessToken, m.PrivateKey, m.PublicKey, m.Fingerprint,
			m.Status, m.StartAt, m.StopAt,
			m.CreatedAt, m.CreatedBy, m.UpdatedAt, m.UpdatedBy,
			m.DeletedAt, m.DeletedBy,
			m.ID, m.Version,
		).Exec()
		if err != nil {
			return nil, err
		}
		n, _ := res.RowsAffected()
		if n == 0 {
			return nil, apperror.Conflict("optimistic_lock_conflict")
		}
		m.Version++
	}
	return clientToDomain(m), nil
}

func (r *OrmClientRepository) SoftDelete(c *domclient.Client) error {
	now := time.Now()
	res, err := r.o.Raw(
		"UPDATE clients SET deleted_at=?, deleted_by=?, version=version+1 WHERE id=? AND version=?",
		now, c.DeletedBy, c.ID, c.Version,
	).Exec()
	if err != nil {
		return err
	}
	n, _ := res.RowsAffected()
	if n == 0 {
		return apperror.Conflict("optimistic_lock_conflict")
	}
	return nil
}

func clientToDomain(m *model.Client) *domclient.Client {
	return &domclient.Client{
		ID:          m.ID,
		Name:        m.Name,
		Identifier:  m.Identifier,
		PostCode:    m.PostCode,
		Pref:        m.Pref,
		City:        m.City,
		Address:     m.Address,
		Building:    m.Building,
		Tel:         m.Tel,
		Email:       m.Email,
		AccessToken: m.AccessToken,
		PrivateKey:  m.PrivateKey,
		PublicKey:   m.PublicKey,
		Fingerprint: m.Fingerprint,
		Status:      m.Status,
		StartAt:     m.StartAt,
		StopAt:      m.StopAt,
		CreatedAt:   m.CreatedAt,
		CreatedBy:   m.CreatedBy,
		UpdatedAt:   m.UpdatedAt,
		UpdatedBy:   m.UpdatedBy,
		DeletedAt:   m.DeletedAt,
		DeletedBy:   m.DeletedBy,
		Version:     m.Version,
	}
}

func clientToModel(c *domclient.Client) *model.Client {
	return &model.Client{
		ID:          c.ID,
		Name:        c.Name,
		Identifier:  c.Identifier,
		PostCode:    c.PostCode,
		Pref:        c.Pref,
		City:        c.City,
		Address:     c.Address,
		Building:    c.Building,
		Tel:         c.Tel,
		Email:       c.Email,
		AccessToken: c.AccessToken,
		PrivateKey:  c.PrivateKey,
		PublicKey:   c.PublicKey,
		Fingerprint: c.Fingerprint,
		Status:      c.Status,
		StartAt:     c.StartAt,
		StopAt:      c.StopAt,
		CreatedAt:   c.CreatedAt,
		CreatedBy:   c.CreatedBy,
		UpdatedAt:   c.UpdatedAt,
		UpdatedBy:   c.UpdatedBy,
		DeletedAt:   c.DeletedAt,
		DeletedBy:   c.DeletedBy,
		Version:     c.Version,
	}
}
