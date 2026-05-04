package persistence

import (
	domstaff "authorization-go/internal/domain/staff"
	"authorization-go/internal/infrastructure/model"
	"authorization-go/internal/support"
	"authorization-go/pkg/apperror"
	"errors"
	"time"

	"gorm.io/gorm"
)

// GormStaffRepository は domain/staff.Repository の GORM 実装です。
type GormStaffRepository struct {
	db *gorm.DB
}

// NewGormStaffRepository は GormStaffRepository を生成します。
//
// db: GORM DB インスタンス
func NewGormStaffRepository(db *gorm.DB) *GormStaffRepository {
	return &GormStaffRepository{db: db}
}

// FindByCondition は検索条件に合致するスタッフエンティティを返します。
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

// FindByID はIDでスタッフエンティティを返します。存在しない場合は nil を返します。
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

// FindByIDUnscoped はIDでスタッフエンティティを返します（論理削除済みも含む）。存在しない場合は nil を返します。
func (r *GormStaffRepository) FindByIDUnscoped(id uint) (*domstaff.Staff, error) {
	var m model.Staff
	if err := r.db.Unscoped().First(&m, id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return staffToDomain(&m), nil
}

// FindByProvider はプロバイダーとプロバイダーIDでスタッフエンティティを返します。
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

// FindAllActive は論理削除されていないスタッフエンティティを全件返します。
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

// Save はスタッフエンティティを保存（新規作成または更新）して返します。
func (r *GormStaffRepository) Save(s *domstaff.Staff) (*domstaff.Staff, error) {
	m := staffToModel(s)
	if err := r.db.Save(m).Error; err != nil {
		return nil, err
	}
	return staffToDomain(m), nil
}

// UpdateRole はスタッフのロールを更新して更新件数の有無を返します。
// version が DB と一致しない場合は楽観排他エラーを返します。
func (r *GormStaffRepository) UpdateRole(id uint, role int, updatedBy uint, version int) (bool, error) {
	now := time.Now()
	result := r.db.Model(&model.Staff{}).
		Where("id = ? AND deleted_at IS NULL AND version = ?", id, version).
		Updates(map[string]interface{}{
			"role":       role,
			"updated_at": now,
			"updated_by": updatedBy,
			"version":    gorm.Expr("version + 1"),
		})
	if result.Error != nil {
		return false, result.Error
	}
	if result.RowsAffected == 0 {
		return false, apperror.Conflict("optimistic_lock")
	}
	return true, nil
}

// SoftDelete はスタッフを論理削除して更新件数の有無を返します。
// version が DB と一致しない場合は楽観排他エラーを返します。
func (r *GormStaffRepository) SoftDelete(id uint, deletedBy uint, version int) (bool, error) {
	now := time.Now()
	result := r.db.Model(&model.Staff{}).
		Where("id = ? AND deleted_at IS NULL AND version = ?", id, version).
		Updates(map[string]interface{}{
			"deleted_at": now,
			"deleted_by": deletedBy,
			"version":    gorm.Expr("version + 1"),
		})
	if result.Error != nil {
		return false, result.Error
	}
	if result.RowsAffected == 0 {
		return false, apperror.Conflict("optimistic_lock")
	}
	return true, nil
}

// Restore はスタッフの論理削除を復元して更新件数の有無を返します。
// version が DB と一致しない場合は楽観排他エラーを返します。
func (r *GormStaffRepository) Restore(id uint, version int) (bool, error) {
	result := r.db.Unscoped().Model(&model.Staff{}).
		Where("id = ? AND deleted_at IS NOT NULL AND version = ?", id, version).
		Updates(map[string]interface{}{
			"deleted_at": nil,
			"deleted_by": nil,
			"version":    gorm.Expr("version + 1"),
		})
	if result.Error != nil {
		return false, result.Error
	}
	if result.RowsAffected == 0 {
		return false, apperror.Conflict("optimistic_lock")
	}
	return true, nil
}

// ---------- マッピングヘルパー ----------

func staffToDomain(m *model.Staff) *domstaff.Staff {
	s := &domstaff.Staff{}
	support.Assign(s, m)
	return s
}

func staffToModel(s *domstaff.Staff) *model.Staff {
	m := &model.Staff{}
	support.Assign(m, s)
	return m
}
