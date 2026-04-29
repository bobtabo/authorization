package gate

type IssueDto struct {
	AccessToken string
	MemberID    string
}

type VerifyDto struct {
	Identifier string
	Token      string
}
