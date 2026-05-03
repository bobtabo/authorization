package persistence

import (
	domstaff "authorization-go-beego/internal/domain/staff"
	"authorization-go-beego/internal/infrastructure/model"
	"authorization-go-beego/pkg/apperror"
	"time"

	"github.com/beego/beego/v2/client/orm"
)

type OrmStaffRepository struct {
	o QueryOrmer
}

func NewOrmStaffRepository(o QueryOrmer) *OrmStaffRepository {
	return &OrmStaffRepository{o: o}
}

func (r *OrmStaffRepository) FindByCondition(cond domstaff.Condition) ([]*domstaff.Staff, error) {
	qs := r.o.QueryTable(new(model.Staff)).OrderBy("id")
	if cond.Keyword != nil && *cond.Keyword != "" {
		kw := *cond.Keyword
		kwCond := orm.NewCondition().And("name__contains", kw).Or("email__contains", kw)
		qs = qs.SetCond(kwCond)
	}
	if len(cond.Roles) > 0 {
		qs = qs.Filter("role__in", cond.Roles)
	}
	var ms []*model.Staff
	if _, err := qs.All(&ms); err != nil {
		return nil, err
	}
	out := make([]*domstaff.Staff, 0, len(ms))
	for _, m := range ms {
		out = append(out, staffToDomain(m))
	}
	return out, nil
}

func (r *OrmStaffRepository) FindByID(s *domstaff.Staff) (*domstaff.Staff, error) {
	var m model.Staff
	err := r.o.QueryTable(new(model.Staff)).
		Filter("id", s.ID).
		Filter("deleted_at__isnull", true).
		One(&m)
	if err == orm.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return staffToDomain(&m), nil
}

func (r *OrmStaffRepository) FindByProvider(s *domstaff.Staff) (*domstaff.Staff, error) {
	var m model.Staff
	err := r.o.QueryTable(new(model.Staff)).
		Filter("provider", s.Provider).
		Filter("provider_id", s.ProviderID).
		One(&m)
	if err == orm.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return staffToDomain(&m), nil
}

func (r *OrmStaffRepository) FindAllActive() ([]*domstaff.Staff, error) {
	var ms []*model.Staff
	if _, err := r.o.QueryTable(new(model.Staff)).
		Filter("deleted_at__isnull", true).
		All(&ms); err != nil {
		return nil, err
	}
	out := make([]*domstaff.Staff, 0, len(ms))
	for _, m := range ms {
		out = append(out, staffToDomain(m))
	}
	return out, nil
}

func (r *OrmStaffRepository) Save(s *domstaff.Staff) (*domstaff.Staff, error) {
	m := staffToModel(s)
	if m.ID == 0 {
		if _, err := r.o.Insert(m); err != nil {
			return nil, err
		}
	} else {
		res, err := r.o.Raw(
			`UPDATE staffs SET
				name=?, email=?, provider=?, provider_id=?, avatar=?, role=?, last_login_at=?,
				created_at=?, created_by=?, updated_at=?, updated_by=?,
				version=version+1
			WHERE id=? AND version=?`,
			m.Name, m.Email, m.Provider, m.ProviderID, m.Avatar, m.Role, m.LastLoginAt,
			m.CreatedAt, m.CreatedBy, m.UpdatedAt, m.UpdatedBy,
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
	return staffToDomain(m), nil
}

func (r *OrmStaffRepository) UpdateRole(s *domstaff.Staff) (bool, error) {
	now := time.Now()
	res, err := r.o.Raw(
		"UPDATE staffs SET role=?, updated_at=?, updated_by=?, version=version+1 WHERE id=? AND version=? AND deleted_at IS NULL",
		s.Role, now, s.UpdatedBy, s.ID, s.Version,
	).Exec()
	if err != nil {
		return false, err
	}
	n, _ := res.RowsAffected()
	if n == 0 {
		return false, apperror.Conflict("optimistic_lock_conflict")
	}
	return true, nil
}

func (r *OrmStaffRepository) SoftDelete(s *domstaff.Staff) (bool, error) {
	now := time.Now()
	res, err := r.o.Raw(
		"UPDATE staffs SET deleted_at=?, deleted_by=?, version=version+1 WHERE id=? AND version=? AND deleted_at IS NULL",
		now, s.DeletedBy, s.ID, s.Version,
	).Exec()
	if err != nil {
		return false, err
	}
	n, _ := res.RowsAffected()
	if n == 0 {
		return false, apperror.Conflict("optimistic_lock_conflict")
	}
	return true, nil
}

func (r *OrmStaffRepository) Restore(s *domstaff.Staff) (bool, error) {
	res, err := r.o.Raw(
		"UPDATE staffs SET deleted_at=NULL, deleted_by=NULL WHERE id=? AND deleted_at IS NOT NULL",
		s.ID,
	).Exec()
	if err != nil {
		return false, err
	}
	n, _ := res.RowsAffected()
	return n > 0, nil
}

func staffToDomain(m *model.Staff) *domstaff.Staff {
	return &domstaff.Staff{
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
		DeletedAt:   m.DeletedAt,
		DeletedBy:   m.DeletedBy,
		Version:     m.Version,
	}
}

func staffToModel(s *domstaff.Staff) *model.Staff {
	return &model.Staff{
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
		DeletedAt:   s.DeletedAt,
		DeletedBy:   s.DeletedBy,
		Version:     s.Version,
	}
}
