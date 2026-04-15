package repository

import (
	"authorization-go/internal/model"
	"errors"
	"time"

	"gorm.io/gorm"
)

type StaffRepository struct {
	db *gorm.DB
}

func NewStaffRepository(db *gorm.DB) *StaffRepository {
	return &StaffRepository{db: db}
}

type StaffFilter struct {
	Keyword  *string
	Roles    []int
	Statuses []int
}

// FindByCondition はスタッフ一覧を取得します。削除済みも含めて返します。
func (r *StaffRepository) FindByCondition(f StaffFilter) ([]*model.Staff, error) {
	q := r.db.Unscoped().Order("id ASC")
	if f.Keyword != nil && *f.Keyword != "" {
		like := "%" + *f.Keyword + "%"
		q = q.Where("name LIKE ? OR email LIKE ?", like, like)
	}
	if len(f.Roles) > 0 {
		q = q.Where("role IN ?", f.Roles)
	}
	var staffs []*model.Staff
	return staffs, q.Find(&staffs).Error
}

func (r *StaffRepository) FindByID(id uint) (*model.Staff, error) {
	var s model.Staff
	if err := r.db.First(&s, id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &s, nil
}

func (r *StaffRepository) FindByProvider(provider int, providerID string) (*model.Staff, error) {
	var s model.Staff
	if err := r.db.Unscoped().Where("provider = ? AND provider_id = ?", provider, providerID).First(&s).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &s, nil
}

func (r *StaffRepository) FindAllActive() ([]*model.Staff, error) {
	var staffs []*model.Staff
	return staffs, r.db.Where("deleted_at IS NULL").Find(&staffs).Error
}

func (r *StaffRepository) Save(s *model.Staff) (*model.Staff, error) {
	return s, r.db.Save(s).Error
}

func (r *StaffRepository) UpdateRole(id uint, role int, updatedBy uint) (bool, error) {
	now := time.Now()
	result := r.db.Model(&model.Staff{}).
		Where("id = ? AND deleted_at IS NULL", id).
		Updates(map[string]interface{}{
			"role":       role,
			"updated_at": now,
			"updated_by": updatedBy,
			"version":    gorm.Expr("version + 1"),
		})
	return result.RowsAffected > 0, result.Error
}

func (r *StaffRepository) SoftDelete(id uint, deletedBy uint) (bool, error) {
	now := time.Now()
	result := r.db.Model(&model.Staff{}).
		Where("id = ? AND deleted_at IS NULL", id).
		Updates(map[string]interface{}{
			"deleted_at": now,
			"deleted_by": deletedBy,
		})
	return result.RowsAffected > 0, result.Error
}

func (r *StaffRepository) Restore(id uint) (bool, error) {
	result := r.db.Unscoped().Model(&model.Staff{}).
		Where("id = ? AND deleted_at IS NOT NULL", id).
		Updates(map[string]interface{}{
			"deleted_at": nil,
			"deleted_by": nil,
		})
	return result.RowsAffected > 0, result.Error
}
