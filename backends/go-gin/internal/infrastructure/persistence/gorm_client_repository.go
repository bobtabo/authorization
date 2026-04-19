package persistence

import (
	domclient "authorization-go/internal/domain/client"
	"authorization-go/internal/infrastructure/model"
	"errors"
	"time"

	"gorm.io/gorm"
)

// GormClientRepository は domain/client.Repository の GORM 実装です。
type GormClientRepository struct {
	db *gorm.DB
}

func NewGormClientRepository(db *gorm.DB) *GormClientRepository {
	return &GormClientRepository{db: db}
}

func (r *GormClientRepository) FindByCondition(cond domclient.Condition) ([]*domclient.Client, error) {
	q := r.db.Unscoped().Order("id ASC")
	if cond.Keyword != nil && *cond.Keyword != "" {
		q = q.Where("name LIKE ?", "%"+*cond.Keyword+"%")
	}
	if cond.StartFrom != nil {
		q = q.Where("start_at >= ?", cond.StartFrom)
	}
	if cond.StartTo != nil {
		q = q.Where("start_at <= ?", cond.StartTo)
	}
	if len(cond.Statuses) > 0 {
		q = q.Where("status IN ?", cond.Statuses)
	}
	var ms []*model.Client
	if err := q.Find(&ms).Error; err != nil {
		return nil, err
	}
	out := make([]*domclient.Client, 0, len(ms))
	for _, m := range ms {
		out = append(out, clientToDomain(m))
	}
	return out, nil
}

func (r *GormClientRepository) FindByID(id uint64) (*domclient.Client, error) {
	var m model.Client
	if err := r.db.Unscoped().First(&m, id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return clientToDomain(&m), nil
}

func (r *GormClientRepository) FindByAccessToken(token string) (*domclient.Client, error) {
	var m model.Client
	if err := r.db.Where("access_token = ? AND status = ?", token, domclient.StatusActive).First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return clientToDomain(&m), nil
}

func (r *GormClientRepository) FindByIdentifier(identifier string) (*domclient.Client, error) {
	var m model.Client
	if err := r.db.Where("identifier = ?", identifier).First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return clientToDomain(&m), nil
}

func (r *GormClientRepository) Save(c *domclient.Client) (*domclient.Client, error) {
	m := clientToModel(c)
	if err := r.db.Save(m).Error; err != nil {
		return nil, err
	}
	return clientToDomain(m), nil
}

func (r *GormClientRepository) SoftDelete(id uint64, deletedBy uint) error {
	now := time.Now()
	return r.db.Model(&model.Client{}).Where("id = ?", id).Updates(map[string]interface{}{
		"deleted_at": now,
		"deleted_by": deletedBy,
	}).Error
}

// ---------- マッピングヘルパー ----------

func clientToDomain(m *model.Client) *domclient.Client {
	c := &domclient.Client{
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
		DeletedBy:   m.DeletedBy,
		Version:     m.Version,
	}
	if m.DeletedAt.Valid {
		c.DeletedAt = &m.DeletedAt.Time
	}
	return c
}

func clientToModel(c *domclient.Client) *model.Client {
	m := &model.Client{
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
		DeletedBy:   c.DeletedBy,
		Version:     c.Version,
	}
	if c.DeletedAt != nil {
		m.DeletedAt = gorm.DeletedAt{Time: *c.DeletedAt, Valid: true}
	}
	return m
}
