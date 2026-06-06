package staff

type Condition struct {
	Keyword  *string
	Roles    []int
	Offset   int
	Limit    int
	Sort     string
	SortType string
}
