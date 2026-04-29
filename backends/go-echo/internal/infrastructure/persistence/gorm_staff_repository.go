// Package persistence はGORMを使ったリポジトリ実装を提供します。
package persistence

import (
	domstaff "authorization-go-echo/internal/domain/staff"
	"authorization-go-echo/internal/infrastructure/model"
	"errors"
	"time"

	"gorm.io/gorm"
)

// GormStaffRepository はGORMを使ったスタッフリポジトリ実装です。
type GormStaffRepository struct {
	db *gorm.DB
}

// NewGormStaffRepository は GormStaffRepository を生成します。
func NewGormStaffRepository(db *gorm.DB) *GormStaffRepository {
	return &GormStaffRepository{db: db}
}

// FindByCondition は条件に合うスタッフ一覧を取得します。
func (r *GormStaffRepository) FindByCondition(cond domstaff.Condition) ([]*domstaff.Staff, error) {
	q := r.db.Unscoped().Order("id ASC")
	if cond.Keyword != nil && *cond.Keyword != "" {
		like := "%" + *cond.Keyword + "%"
		q = q.Where("name LIKE ? OR email LIKE ?", like, like)
	}
	if len(cond.Roles) > 0 {
		q = q.Where("role IN ?", cond.Roles)
	}
	var ms []*model.Staff
	if err := q.Find(&ms).Error; err != nil {
		return nil, err
	}
	out := make([]*domstaff.Staff, 0, len(ms))
	for _, m := range ms {
		out = append(out, staffToDomain(m))
	}
	return out, nil
}

// FindByID はIDでスタッフを取得します。
func (r *GormStaffRepository) FindByID(s *domstaff.Staff) (*domstaff.Staff, error) {
	var m model.Staff
	if err := r.db.First(&m, s.ID).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return staffToDomain(&m), nil
}

// FindByProvider はOAuthプロバイダー情報でスタッフを取得します。
func (r *GormStaffRepository) FindByProvider(s *domstaff.Staff) (*domstaff.Staff, error) {
	var m model.Staff
	if err := r.db.Unscoped().Where("provider = ? AND provider_id = ?", s.Provider, s.ProviderID).First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return staffToDomain(&m), nil
}

// FindAllActive は論理削除されていない全スタッフを取得します。
func (r *GormStaffRepository) FindAllActive() ([]*domstaff.Staff, error) {
	var ms []*model.Staff
	if err := r.db.Where("deleted_at IS NULL").Find(&ms).Error; err != nil {
		return nil, err
	}
	out := make([]*domstaff.Staff, 0, len(ms))
	for _, m := range ms {
		out = append(out, staffToDomain(m))
	}
	return out, nil
}

// Save はスタッフを登録または更新します。
func (r *GormStaffRepository) Save(s *domstaff.Staff) (*domstaff.Staff, error) {
	m := staffToModel(s)
	if err := r.db.Save(m).Error; err != nil {
		return nil, err
	}
	return staffToDomain(m), nil
}

// UpdateRole はスタッフのロールを更新します。
func (r *GormStaffRepository) UpdateRole(s *domstaff.Staff) (bool, error) {
	now := time.Now()
	result := r.db.Model(&model.Staff{}).Where("id = ? AND deleted_at IS NULL", s.ID).Updates(map[string]interface{}{
		"role":       s.Role,
		"updated_at": now,
		"updated_by": s.UpdatedBy,
		"version":    gorm.Expr("version + 1"),
	})
	return result.RowsAffected > 0, result.Error
}

// SoftDelete はスタッフを論理削除します。
func (r *GormStaffRepository) SoftDelete(s *domstaff.Staff) (bool, error) {
	now := time.Now()
	result := r.db.Model(&model.Staff{}).Where("id = ? AND deleted_at IS NULL", s.ID).Updates(map[string]interface{}{
		"deleted_at": now,
		"deleted_by": s.DeletedBy,
	})
	return result.RowsAffected > 0, result.Error
}

// Restore は論理削除したスタッフを復元します。
func (r *GormStaffRepository) Restore(s *domstaff.Staff) (bool, error) {
	result := r.db.Unscoped().Model(&model.Staff{}).Where("id = ? AND deleted_at IS NOT NULL", s.ID).Updates(map[string]interface{}{
		"deleted_at": nil,
		"deleted_by": nil,
	})
	return result.RowsAffected > 0, result.Error
}

func staffToDomain(m *model.Staff) *domstaff.Staff {
	s := &domstaff.Staff{
		ID:          m.ID,
		Name:        m.Name,
		Email:       m.Email,
		Provider:    m.Provider,
		ProviderID:  m.ProviderID,
		Avatar:      m.Avatar,
		Role:        m.Role,
		LastLoginAt: m.LastLoginAt,
		CreatedAt:   m.CreatedAt,
		CreatedBy:   m.CreatedBy,
		UpdatedAt:   m.UpdatedAt,
		UpdatedBy:   m.UpdatedBy,
		DeletedBy:   m.DeletedBy,
		Version:     m.Version,
	}
	if m.DeletedAt.Valid {
		s.DeletedAt = &m.DeletedAt.Time
	}
	return s
}

func staffToModel(s *domstaff.Staff) *model.Staff {
	m := &model.Staff{
		ID:          s.ID,
		Name:        s.Name,
		Email:       s.Email,
		Provider:    s.Provider,
		ProviderID:  s.ProviderID,
		Avatar:      s.Avatar,
		Role:        s.Role,
		LastLoginAt: s.LastLoginAt,
		CreatedAt:   s.CreatedAt,
		CreatedBy:   s.CreatedBy,
		UpdatedAt:   s.UpdatedAt,
		UpdatedBy:   s.UpdatedBy,
		DeletedBy:   s.DeletedBy,
		Version:     s.Version,
	}
	if s.DeletedAt != nil {
		m.DeletedAt = gorm.DeletedAt{Time: *s.DeletedAt, Valid: true}
	}
	return m
}
