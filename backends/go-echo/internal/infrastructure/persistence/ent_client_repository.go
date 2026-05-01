package persistence

import (
	"authorization-go-echo/ent"
	"authorization-go-echo/ent/appclient"
	domclient "authorization-go-echo/internal/domain/client"
	"context"
	"time"
)

type EntClientRepository struct {
	db *ent.Client
}

func NewEntClientRepository(db *ent.Client) *EntClientRepository {
	return &EntClientRepository{db: db}
}

func (r *EntClientRepository) FindByCondition(cond domclient.Condition) ([]*domclient.Client, error) {
	q := r.db.AppClient.Query().Order(ent.Asc(appclient.FieldID))
	if cond.Keyword != nil && *cond.Keyword != "" {
		q = q.Where(appclient.NameContains(*cond.Keyword))
	}
	if cond.StartFrom != nil {
		q = q.Where(appclient.StartAtGTE(*cond.StartFrom))
	}
	if cond.StartTo != nil {
		q = q.Where(appclient.StartAtLTE(*cond.StartTo))
	}
	if len(cond.Statuses) > 0 {
		q = q.Where(appclient.StatusIn(cond.Statuses...))
	}
	ms, err := q.All(context.Background())
	if err != nil {
		return nil, err
	}
	out := make([]*domclient.Client, 0, len(ms))
	for _, m := range ms {
		out = append(out, entAppClientToDomain(m))
	}
	return out, nil
}

func (r *EntClientRepository) FindByID(c *domclient.Client) (*domclient.Client, error) {
	m, err := r.db.AppClient.Get(context.Background(), c.ID)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil, nil
		}
		return nil, err
	}
	return entAppClientToDomain(m), nil
}

func (r *EntClientRepository) FindByAccessToken(c *domclient.Client) (*domclient.Client, error) {
	m, err := r.db.AppClient.Query().
		Where(appclient.AccessTokenEQ(c.AccessToken), appclient.StatusEQ(domclient.StatusActive), appclient.DeletedAtIsNil()).
		First(context.Background())
	if err != nil {
		if ent.IsNotFound(err) {
			return nil, nil
		}
		return nil, err
	}
	return entAppClientToDomain(m), nil
}

func (r *EntClientRepository) FindByIdentifier(c *domclient.Client) (*domclient.Client, error) {
	m, err := r.db.AppClient.Query().
		Where(appclient.IdentifierEQ(c.Identifier), appclient.DeletedAtIsNil()).
		First(context.Background())
	if err != nil {
		if ent.IsNotFound(err) {
			return nil, nil
		}
		return nil, err
	}
	return entAppClientToDomain(m), nil
}

func (r *EntClientRepository) Save(c *domclient.Client) (*domclient.Client, error) {
	ctx := context.Background()
	now := time.Now()
	if c.ID == 0 {
		q := r.db.AppClient.Create().
			SetName(c.Name).
			SetIdentifier(c.Identifier).
			SetPostCode(c.PostCode).
			SetPref(c.Pref).
			SetCity(c.City).
			SetAddress(c.Address).
			SetBuilding(c.Building).
			SetTel(c.Tel).
			SetEmail(c.Email).
			SetAccessToken(c.AccessToken).
			SetPrivateKey(c.PrivateKey).
			SetPublicKey(c.PublicKey).
			SetFingerprint(c.Fingerprint).
			SetStatus(c.Status).
			SetNillableStartAt(c.StartAt).
			SetNillableStopAt(c.StopAt).
			SetCreatedAt(now).
			SetNillableCreatedBy(uintPtrToNillable(c.CreatedBy)).
			SetUpdatedAt(now).
			SetNillableUpdatedBy(uintPtrToNillable(c.UpdatedBy)).
			SetVersion(c.Version)
		m, err := q.Save(ctx)
		if err != nil {
			return nil, err
		}
		return entAppClientToDomain(m), nil
	}
	q := r.db.AppClient.UpdateOneID(c.ID).
		SetName(c.Name).
		SetIdentifier(c.Identifier).
		SetPostCode(c.PostCode).
		SetPref(c.Pref).
		SetCity(c.City).
		SetAddress(c.Address).
		SetBuilding(c.Building).
		SetTel(c.Tel).
		SetEmail(c.Email).
		SetAccessToken(c.AccessToken).
		SetPrivateKey(c.PrivateKey).
		SetPublicKey(c.PublicKey).
		SetFingerprint(c.Fingerprint).
		SetStatus(c.Status).
		SetNillableStartAt(c.StartAt).
		SetNillableStopAt(c.StopAt).
		SetUpdatedAt(now).
		SetNillableUpdatedBy(uintPtrToNillable(c.UpdatedBy)).
		AddVersion(1)
	m, err := q.Save(ctx)
	if err != nil {
		return nil, err
	}
	return entAppClientToDomain(m), nil
}

func (r *EntClientRepository) SoftDelete(c *domclient.Client) error {
	now := time.Now()
	return r.db.AppClient.UpdateOneID(c.ID).
		SetDeletedAt(now).
		SetNillableDeletedBy(uintPtrToNillable(c.DeletedBy)).
		Exec(context.Background())
}

func entAppClientToDomain(m *ent.AppClient) *domclient.Client {
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

func uintPtrToNillable(p *uint) *uint {
	return p
}
