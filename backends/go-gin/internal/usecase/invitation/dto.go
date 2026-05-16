package invitation

// CurrentDto は現在の招待情報取得のユースケース入力です。
type CurrentDto struct {
	Role int
}

// IssueDto は招待発行のユースケース入力です。
type IssueDto struct {
	Role int
}

// FindByTokenDto は招待トークン検索のユースケース入力です。
type FindByTokenDto struct {
	Token string
}
