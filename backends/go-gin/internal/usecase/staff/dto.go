package staff

// UpdateRoleDto はスタッフロール更新のユースケース入力です。
type UpdateRoleDto struct {
	ID         uint
	Role       int
	ExecutorID uint
}

// DestroyDto はスタッフ論理削除のユースケース入力です。
type DestroyDto struct {
	ID         uint
	ExecutorID uint
}
