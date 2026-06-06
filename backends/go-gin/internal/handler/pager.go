package handler

import "math"

// defaultPageCount はページャー番号ボタンの表示数です。
const defaultPageCount = 5

// Pager はサーバーサイドページングのレスポンス構造体です。
type Pager struct {
	Count            int  `json:"count"`
	Limit            int  `json:"limit"`
	Next             bool `json:"next"`
	Previous         bool `json:"previous"`
	Page             int  `json:"page"`
	NextPage         int  `json:"nextPage"`
	PreviousPage     int  `json:"previousPage"`
	PageCount        int  `json:"pageCount"`
	First            bool `json:"first"`
	Last             bool `json:"last"`
	FirstRecordCount int  `json:"firstRecordCount"`
	LastRecordCount  int  `json:"lastRecordCount"`
	StartPage        int  `json:"startPage"`
	EndPage          int  `json:"endPage"`
}

// BuildPager はページング情報を構築します。
func BuildPager(count, limit, offset, recordCount int) Pager {
	if limit <= 0 {
		limit = 20
	}

	pageCount := int(math.Ceil(float64(count) / float64(limit)))
	if pageCount < 1 {
		pageCount = 1
	}

	lastPageOffset := (pageCount * limit) - limit
	if count > 0 && offset > lastPageOffset {
		offset = lastPageOffset
	}

	page := int(math.Floor(math.Ceil(float64(offset)/float64(limit)))) + 1

	startPage := page - (defaultPageCount - 1)
	if startPage <= 0 {
		startPage = 1
	}
	endPage := startPage + (defaultPageCount - 1)
	if endPage > pageCount {
		endPage = pageCount
	}

	return Pager{
		Count:            count,
		Limit:            limit,
		Next:             pageCount > page,
		Previous:         page > 1,
		Page:             page,
		NextPage:         page + 1,
		PreviousPage:     page - 1,
		PageCount:        pageCount,
		First:            page > 1,
		Last:             pageCount > page,
		FirstRecordCount: offset + 1,
		LastRecordCount:  offset + recordCount,
		StartPage:        startPage,
		EndPage:          endPage,
	}
}
