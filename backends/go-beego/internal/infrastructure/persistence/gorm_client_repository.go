package persistence

import (
	domclient "authorization-go-beego/internal/domain/client"
	"authorization-go-beego/internal/infrastructure/model"
	"authorization-go-beego/internal/support"
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
		var startAt, stopAt, createdBy, updatedBy interface{}
		if m.StartAt != nil {
			startAt = m.StartAt
		}
		if m.StopAt != nil {
			stopAt = m.StopAt
		}
		if m.CreatedBy != nil {
			createdBy = m.CreatedBy
		}
		if m.UpdatedBy != nil {
			updatedBy = m.UpdatedBy
		}
		res, err := r.o.Raw(
			`UPDATE clients SET
				name=?, identifier=?, post_code=?, pref=?, city=?, address=?, building=?,
				tel=?, email=?, access_token=?, private_key=?, public_key=?, fingerprint=?,
				status=?, start_at=?, stop_at=?,
				created_at=?, created_by=?, updated_at=?, updated_by=?,
				version=version+1
			WHERE id=? AND version=?`,
			m.Name, m.Identifier, m.PostCode, m.Pref, m.City, m.Address, m.Building,
			m.Tel, m.Email, m.AccessToken, m.PrivateKey, m.PublicKey, m.Fingerprint,
			m.Status, startAt, stopAt,
			m.CreatedAt, createdBy, m.UpdatedAt, updatedBy,
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
	c := &domclient.Client{}
	support.Assign(c, m)
	return c
}

func clientToModel(c *domclient.Client) *model.Client {
	m := &model.Client{}
	support.Assign(m, c)
	return m
}
