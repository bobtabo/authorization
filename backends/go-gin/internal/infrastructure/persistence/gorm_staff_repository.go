package persistence

import (
	domstaff "authorization-go/internal/domain/staff"
	"authorization-go/internal/infrastructure/model"
	"errors"
	"time"

	"gorm.io/gorm"
)

// GormStaffRepository は domain/staff.Repository の GORM 実装です。
type GormStaffRepository struct {
	db *gorm.DB
}

func NewGormStaffRepository(db *gorm.DB) *GormStaffRepository {
	return &GormStaffRepository{db: db}
}

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

func (r *GormStaffRepository) FindByID(id uint) (*domstaff.Staff, error) {
	var m model.Staff
	if err := r.db.First(&m, id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return staffToDomain(&m), nil
}

func (r *GormStaffRepository) FindByProvider(provider int, providerID string) (*domstaff.Staff, error) {
	var m model.Staff
	if err := r.db.Unscoped().Where("provider = ? AND provider_id = ?", provider, providerID).First(&m).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return staffToDomain(&m), nil
}

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

func (r *GormStaffRepository) Save(s *domstaff.Staff) (*domstaff.Staff, error) {
	m := staffToModel(s)
	if err := r.db.Save(m).Error; err != nil {
		return nil, err
	}
	return staffToDomain(m), nil
}

func (r *GormStaffRepository) UpdateRole(id uint, role int, updatedBy uint) (bool, error) {
	now := time.Now()
	result := r.db.Model(&model.Staff{}).Where("id = ? AND deleted_at IS NULL", id).Updates(map[string]interface{}{
		"role":       role,
		"updated_at": now,
		"updated_by": updatedBy,
		"version":    gorm.Expr("version + 1"),
	})
	return result.RowsAffected > 0, result.Error
}

func (r *GormStaffRepository) SoftDelete(id uint, deletedBy uint) (bool, error) {
	now := time.Now()
	result := r.db.Model(&model.Staff{}).Where("id = ? AND deleted_at IS NULL", id).Updates(map[string]interface{}{
		"deleted_at": now,
		"deleted_by": deletedBy,
	})
	return result.RowsAffected > 0, result.Error
}

func (r *GormStaffRepository) Restore(id uint) (bool, error) {
	result := r.db.Unscoped().Model(&model.Staff{}).Where("id = ? AND deleted_at IS NOT NULL", id).Updates(map[string]interface{}{
		"deleted_at": nil,
		"deleted_by": nil,
	})
	return result.RowsAffected > 0, result.Error
}

// ---------- マッピングヘルパー ----------

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
