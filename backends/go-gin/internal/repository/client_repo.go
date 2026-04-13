package repository

import (
	"authorization-go/internal/model"
	"errors"
	"time"

	"gorm.io/gorm"
)

type ClientRepository struct {
	db *gorm.DB
}

func NewClientRepository(db *gorm.DB) *ClientRepository {
	return &ClientRepository{db: db}
}

type ClientFilter struct {
	Keyword   *string
	StartFrom *time.Time
	StartTo   *time.Time
	Statuses  []int
}

func (r *ClientRepository) FindByCondition(f ClientFilter) ([]*model.Client, error) {
	q := r.db.Order("id ASC")
	if f.Keyword != nil && *f.Keyword != "" {
		q = q.Where("name LIKE ?", "%"+*f.Keyword+"%")
	}
	if f.StartFrom != nil {
		q = q.Where("start_at >= ?", f.StartFrom)
	}
	if f.StartTo != nil {
		q = q.Where("start_at <= ?", f.StartTo)
	}
	if len(f.Statuses) > 0 {
		q = q.Where("status IN ?", f.Statuses)
	}
	var clients []*model.Client
	return clients, q.Find(&clients).Error
}

func (r *ClientRepository) FindByID(id uint64) (*model.Client, error) {
	var c model.Client
	if err := r.db.First(&c, id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &c, nil
}

func (r *ClientRepository) FindByAccessToken(token string) (*model.Client, error) {
	var c model.Client
	if err := r.db.Where("access_token = ? AND status = ?", token, model.ClientStatusActive).First(&c).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &c, nil
}

func (r *ClientRepository) FindByIdentifier(identifier string) (*model.Client, error) {
	var c model.Client
	if err := r.db.Where("identifier = ?", identifier).First(&c).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &c, nil
}

func (r *ClientRepository) Save(c *model.Client) (*model.Client, error) {
	return c, r.db.Save(c).Error
}

func (r *ClientRepository) SoftDelete(id uint64, deletedBy uint) error {
	now := time.Now()
	return r.db.Model(&model.Client{}).
		Where("id = ?", id).
		Updates(map[string]interface{}{
			"deleted_at": now,
			"deleted_by": deletedBy,
		}).Error
}
