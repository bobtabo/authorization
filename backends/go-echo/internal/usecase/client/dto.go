package client

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

type ListConditionDto struct {
	Keyword   *string
	StartFrom *string
	StartTo   *string
	Statuses  []int
}
