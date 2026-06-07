package persistence

import (
	"authorization-go-echo/ent"
	"authorization-go-echo/ent/staff"
	domstaff "authorization-go-echo/internal/domain/staff"
	"authorization-go-echo/internal/support"
	"context"
	"time"
)

type EntStaffRepository struct {
	db *ent.Client
}

func NewEntStaffRepository(db *ent.Client) *EntStaffRepository {
	return &EntStaffRepository{db: db}
}

func (r *EntStaffRepository) CountByCondition(cond domstaff.Condition) (int, error) {
	q := r.db.Staff.Query()
	q = r.applyFilters(q, cond)
	return q.Count(context.Background())
}

func (r *EntStaffRepository) FindByCondition(cond domstaff.Condition) ([]*domstaff.Staff, error) {
	q := r.db.Staff.Query()
	q = r.applyFilters(q, cond)

	if cond.Limit > 0 {
		q = q.Limit(cond.Limit).Offset(cond.Offset)
	}

	allowedSort := map[string]string{
		"name":       staff.FieldName,
		"role":       staff.FieldRole,
		"created_at": staff.FieldCreatedAt,
	}
	sortField := staff.FieldID
	if f, ok := allowedSort[cond.Sort]; ok {
		sortField = f
	}
	if cond.SortType == "desc" {
		q = q.Order(ent.Desc(sortField))
	} else {
		q = q.Order(ent.Asc(sortField))
	}

	ms, err := q.All(context.Background())
	if err != nil {
		return nil, err
	}
	out := make([]*domstaff.Staff, 0, len(ms))
	for _, m := range ms {
		out = append(out, entStaffToDomain(m))
	}
	return out, nil
}

func (r *EntStaffRepository) applyFilters(q *ent.StaffQuery, cond domstaff.Condition) *ent.StaffQuery {
	if cond.Keyword != nil && *cond.Keyword != "" {
		like := *cond.Keyword
		q = q.Where(staff.Or(staff.NameContains(like), staff.EmailContains(like)))
	}
	if len(cond.Roles) > 0 {
		q = q.Where(staff.RoleIn(cond.Roles...))
	}
	return q
}

func (r *EntStaffRepository) FindByID(s *domstaff.Staff) (*domstaff.Staff, error) {
	m, err := r.db.Staff.Query().
		Where(staff.IDEQ(s.ID), staff.DeletedAtIsNil()).
		First(context.Background())
	if err != nil {
		if ent.IsNotFound(err) {
			return nil, nil
		}
		return nil, err
	}
	return entStaffToDomain(m), nil
}

func (r *EntStaffRepository) FindByIDIncludeDeleted(s *domstaff.Staff) (*domstaff.Staff, error) {
	m, err := r.db.Staff.Query().
		Where(staff.IDEQ(s.ID)).
		First(context.Background())
	if err != nil {
		if ent.IsNotFound(err) {
			return nil, nil
		}
		return nil, err
	}
	return entStaffToDomain(m), nil
}

func (r *EntStaffRepository) FindByProvider(s *domstaff.Staff) (*domstaff.Staff, error) {
	m, err := r.db.Staff.Query().
		Where(staff.ProviderEQ(s.Provider), staff.ProviderIDEQ(s.ProviderID)).
		First(context.Background())
	if err != nil {
		if ent.IsNotFound(err) {
			return nil, nil
		}
		return nil, err
	}
	return entStaffToDomain(m), nil
}

func (r *EntStaffRepository) FindAllActive() ([]*domstaff.Staff, error) {
	ms, err := r.db.Staff.Query().
		Where(staff.DeletedAtIsNil()).
		All(context.Background())
	if err != nil {
		return nil, err
	}
	out := make([]*domstaff.Staff, 0, len(ms))
	for _, m := range ms {
		out = append(out, entStaffToDomain(m))
	}
	return out, nil
}

func (r *EntStaffRepository) Save(s *domstaff.Staff) (*domstaff.Staff, error) {
	ctx := context.Background()
	now := time.Now()
	if s.ID == 0 {
		q := r.db.Staff.Create().
			SetName(s.Name).
			SetEmail(s.Email).
			SetProvider(s.Provider).
			SetProviderID(s.ProviderID).
			SetNillableAvatar(s.Avatar).
			SetRole(s.Role).
			SetNillableLastLoginAt(s.LastLoginAt).
			SetCreatedAt(now).
			SetNillableCreatedBy(uintPtrToNillable(s.CreatedBy)).
			SetUpdatedAt(now).
			SetNillableUpdatedBy(uintPtrToNillable(s.UpdatedBy)).
			SetVersion(s.Version)
		m, err := q.Save(ctx)
		if err != nil {
			return nil, err
		}
		return entStaffToDomain(m), nil
	}
	q := r.db.Staff.UpdateOneID(s.ID).
		SetName(s.Name).
		SetEmail(s.Email).
		SetProvider(s.Provider).
		SetProviderID(s.ProviderID).
		SetNillableAvatar(s.Avatar).
		SetRole(s.Role).
		SetNillableLastLoginAt(s.LastLoginAt).
		SetUpdatedAt(now).
		SetNillableUpdatedBy(uintPtrToNillable(s.UpdatedBy)).
		AddVersion(1)
	m, err := q.Save(ctx)
	if err != nil {
		return nil, err
	}
	return entStaffToDomain(m), nil
}

func (r *EntStaffRepository) UpdateRole(s *domstaff.Staff) (bool, error) {
	now := time.Now()
	n, err := r.db.Staff.Update().
		Where(staff.IDEQ(s.ID), staff.DeletedAtIsNil()).
		SetRole(s.Role).
		SetUpdatedAt(now).
		SetNillableUpdatedBy(uintPtrToNillable(s.UpdatedBy)).
		AddVersion(1).
		Save(context.Background())
	return n > 0, err
}

func (r *EntStaffRepository) SoftDelete(s *domstaff.Staff) (bool, error) {
	now := time.Now()
	n, err := r.db.Staff.Update().
		Where(staff.IDEQ(s.ID), staff.DeletedAtIsNil()).
		SetDeletedAt(now).
		SetNillableDeletedBy(uintPtrToNillable(s.DeletedBy)).
		Save(context.Background())
	return n > 0, err
}

func (r *EntStaffRepository) Restore(s *domstaff.Staff) (bool, error) {
	n, err := r.db.Staff.Update().
		Where(staff.IDEQ(s.ID), staff.DeletedAtNotNil()).
		ClearDeletedAt().
		ClearDeletedBy().
		Save(context.Background())
	return n > 0, err
}

func entStaffToDomain(m *ent.Staff) *domstaff.Staff {
	s := &domstaff.Staff{}
	support.Assign(s, m)
	return s
}
