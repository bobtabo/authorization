// Package staff はスタッフドメインのインターフェースを定義します。
package staff

// Repository はスタッフの永続化インターフェースです。
type Repository interface {
	// FindByCondition は条件に合うスタッフ一覧を取得します。
	FindByCondition(cond Condition) ([]*Staff, error)
	// FindByID はIDでスタッフを取得します。
	FindByID(s *Staff) (*Staff, error)
	// FindByProvider はOAuthプロバイダー情報でスタッフを取得します。
	FindByProvider(s *Staff) (*Staff, error)
	// FindAllActive は論理削除されていない全スタッフを取得します。
	FindAllActive() ([]*Staff, error)
	// Save はスタッフを登録または更新します。
	Save(s *Staff) (*Staff, error)
	// UpdateRole はスタッフのロールを更新します。
	UpdateRole(s *Staff) (bool, error)
	// SoftDelete はスタッフを論理削除します。
	SoftDelete(s *Staff) (bool, error)
	// Restore は論理削除したスタッフを復元します。
	Restore(s *Staff) (bool, error)
}
