// Package staff はスタッフユースケースの入出力データを定義します。
package staff

// UpdateRoleDto はスタッフロール変更の入力データです。
type UpdateRoleDto struct {
	// ID はスタッフID。
	ID uint
	// Role は変更後のロール。
	Role int
	// ExecutorID は操作を実行したスタッフID。
	ExecutorID uint
	// Version は楽観排他ロック用バージョン番号。
	Version int
}

// DestroyDto はスタッフ削除の入力データです。
type DestroyDto struct {
	// ID はスタッフID。
	ID uint
	// ExecutorID は操作を実行したスタッフID。
	ExecutorID uint
	// Version は楽観排他ロック用バージョン番号。
	Version int
}

// RestoreDto はスタッフ復元の入力データです。
type RestoreDto struct {
	// ID はスタッフID。
	ID uint
	// Version は楽観排他ロック用バージョン番号。
	Version int
}
