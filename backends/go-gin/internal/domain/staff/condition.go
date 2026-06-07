package staff

// Condition はスタッフ検索条件です。
type Condition struct {
	Keyword  *string
	Roles    []int
	Offset   int
	Limit    int
	Sort     string
	SortType string
}
