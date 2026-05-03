package staff

// Repository はスタッフの永続化インターフェースです。
type Repository interface {
	// FindByCondition は検索条件に合致するスタッフエンティティを返します。
	FindByCondition(cond Condition) ([]*Staff, error)
	// FindByID はIDでスタッフエンティティを返します。存在しない場合は nil を返します。
	FindByID(id uint) (*Staff, error)
	// FindByIDUnscoped はIDでスタッフエンティティを返します（論理削除済みも含む）。存在しない場合は nil を返します。
	FindByIDUnscoped(id uint) (*Staff, error)
	// FindByProvider はプロバイダーとプロバイダーIDでスタッフエンティティを返します。
	FindByProvider(provider int, providerID string) (*Staff, error)
	// FindAllActive は論理削除されていないスタッフエンティティを全件返します。
	FindAllActive() ([]*Staff, error)
	// Save はスタッフエンティティを保存（新規作成または更新）して返します。
	Save(s *Staff) (*Staff, error)
	// UpdateRole はスタッフのロールを更新して更新件数の有無を返します。version が DB と一致しない場合は楽観排他エラーを返します。
	UpdateRole(id uint, role int, updatedBy uint, version int) (bool, error)
	// SoftDelete はスタッフを論理削除して更新件数の有無を返します。version が DB と一致しない場合は楽観排他エラーを返します。
	SoftDelete(id uint, deletedBy uint, version int) (bool, error)
	// Restore はスタッフの論理削除を復元して更新件数の有無を返します。version が DB と一致しない場合は楽観排他エラーを返します。
	Restore(id uint, version int) (bool, error)
}
