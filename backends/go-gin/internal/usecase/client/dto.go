package client

// StoreDto はクライアント新規登録のユースケース入力です。
type StoreDto struct {
	Name       string
	PostCode   string
	Pref       string
	City       string
	Address    string
	Building   string
	Tel        string
	Email      string
	ExecutorID uint
}

// UpdateDto はクライアント更新のユースケース入力です。
type UpdateDto struct {
	ID         uint64
	Name       *string
	PostCode   *string
	Pref       *string
	City       *string
	Address    *string
	Building   *string
	Tel        *string
	Email      *string
	Status     *int
	ExecutorID uint
}

// ListConditionDto はクライアント検索条件のユースケース入力です。
type ListConditionDto struct {
	Keyword   *string
	StartFrom *string // "2006-01-02" 形式
	StartTo   *string // "2006-01-02" 形式
	Statuses  []int
}
