package gate

// IssueDto は JWT 発行のユースケース入力です。
type IssueDto struct {
	AccessToken string
	MemberID    string
}

// VerifyDto は JWT 検証のユースケース入力です。
type VerifyDto struct {
	Identifier string
	Token      string
}
