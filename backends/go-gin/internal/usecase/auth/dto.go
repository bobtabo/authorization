package auth

// LoginDto はソーシャルログインのユースケース入力です。
type LoginDto struct {
	Provider   int
	ProviderID string
	Name       string
	Email      string
	Avatar     *string
}
