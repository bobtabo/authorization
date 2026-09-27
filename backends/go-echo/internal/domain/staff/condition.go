package staff

type Condition struct {
	Keyword  *string
	Roles    []int
	Statuses []int
	Offset   int
	Limit    int
	Sort     string
	SortType string
}
