// Package auth は認証ユースケースの入出力データを定義します。
package auth

// FindUserDto はスタッフ取得の入力データです。
type FindUserDto struct {
	// ID はスタッフID。
	ID uint
}

// LoginDto はSSOログインの入力データです。
type LoginDto struct {
	// Provider はOAuthプロバイダー種別。
	Provider int
	// ProviderID はプロバイダーが発行したユーザーID。
	ProviderID string
	// Name はスタッフ名。
	Name string
	// Email はメールアドレス。
	Email string
	// Avatar はアバター画像URL。
	Avatar *string
	// InvitationToken は招待トークン（新規登録時のみ必須）。
	InvitationToken string
}
