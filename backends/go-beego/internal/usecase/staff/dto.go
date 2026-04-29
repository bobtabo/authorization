package staff

type UpdateRoleDto struct {
	ID         uint
	Role       int
	ExecutorID uint
}

type DestroyDto struct {
	ID         uint
	ExecutorID uint
}
