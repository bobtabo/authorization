package staff

// Repository はスタッフの永続化インターフェースです。
type Repository interface {
	FindByCondition(cond Condition) ([]*Staff, error)
	FindByID(id uint) (*Staff, error)
	FindByProvider(provider int, providerID string) (*Staff, error)
	FindAllActive() ([]*Staff, error)
	Save(s *Staff) (*Staff, error)
	UpdateRole(id uint, role int, updatedBy uint) (bool, error)
	SoftDelete(id uint, deletedBy uint) (bool, error)
	Restore(id uint) (bool, error)
}
