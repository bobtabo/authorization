// Package persistence はGORMを使ったリポジトリ実装を提供します。
package persistence

import (
	domclient "authorization-go-beego/internal/domain/client"
	"authorization-go-beego/internal/infrastructure/model"
	"errors"
	"time"

	"gorm.io/gorm"
)

// GormClientRepository はGORMを使ったクライアントリポジトリ実装です。
type GormClientRepository struct {
	db *gorm.DB
}

// NewGormClientRepository は GormClientRepository を生成します。
func NewGormClientRepository(db *gorm.DB) *GormClientRepository {
	return &GormClientRepository{db: db}
}

// FindByCondition は条件に合うクライアント一覧を取得します。
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

// FindByID はIDでクライアントを取得します。
func (r *GormClientRepository) FindByID(c *domclient.Client) (*domclient.Client, error) {
	var m model.Client
	if err := r.db.Unscoped().First(&m, c.ID).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return clientToDomain(&m), nil
}

// FindByAccessToken はアクセストークンでアクティブなクライアントを取得します。
func (r *GormClientRepository) FindByAccessToken(c *domclient.Client) (*domclient.Client, error) {
	var m model.Client
	if err := r.db.Where("access_token = ? AND status = ?", c.AccessToken, domclient.StatusActive).First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return clientToDomain(&m), nil
}

// FindByIdentifier はidentifierでクライアントを取得します。
func (r *GormClientRepository) FindByIdentifier(c *domclient.Client) (*domclient.Client, error) {
	var m model.Client
	if err := r.db.Where("identifier = ?", c.Identifier).First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return clientToDomain(&m), nil
}

// Save はクライアントを登録または更新します。
func (r *GormClientRepository) Save(c *domclient.Client) (*domclient.Client, error) {
	m := clientToModel(c)
	if err := r.db.Save(m).Error; err != nil {
		return nil, err
	}
	return clientToDomain(m), nil
}

// SoftDelete はクライアントを論理削除します。
func (r *GormClientRepository) SoftDelete(c *domclient.Client) error {
	now := time.Now()
	return r.db.Model(&model.Client{}).Where("id = ?", c.ID).Updates(map[string]interface{}{
		"deleted_at": now,
		"deleted_by": c.DeletedBy,
	}).Error
}

// clientToDomain はモデルをドメインエンティティに変換します。
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

// clientToModel はドメインエンティティをモデルに変換します。
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
