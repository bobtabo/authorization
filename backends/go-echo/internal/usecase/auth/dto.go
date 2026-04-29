package auth

type LoginDto struct {
	Provider        int
	ProviderID      string
	Name            string
	Email           string
	Avatar          *string
	InvitationToken string
}
