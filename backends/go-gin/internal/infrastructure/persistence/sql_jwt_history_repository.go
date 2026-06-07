package persistence

import (
	domclient "authorization-go/internal/domain/client"
	"database/sql"
	"fmt"
	"strings"
	"time"
)

var jwtHistorySortWhitelist = map[string]bool{
	"issue_at":  true,
	"member_id": true,
}

// SQLJwtHistoryRepository は生 SQL で jwt_histories を操作するリポジトリです。
type SQLJwtHistoryRepository struct {
	db *sql.DB
}

func NewSQLJwtHistoryRepository(db *sql.DB) *SQLJwtHistoryRepository {
	return &SQLJwtHistoryRepository{db: db}
}

func (r *SQLJwtHistoryRepository) CountByCondition(cond domclient.JwtHistoryCondition) (int, error) {
	var count int
	err := r.db.QueryRow(
		"SELECT COUNT(*) FROM jwt_histories WHERE client_id = ? AND deleted_at IS NULL",
		cond.ClientID,
	).Scan(&count)
	return count, err
}

func (r *SQLJwtHistoryRepository) FindByCondition(cond domclient.JwtHistoryCondition) ([]*domclient.JwtHistory, error) {
	sortCol := "issue_at"
	if jwtHistorySortWhitelist[cond.Sort] {
		sortCol = cond.Sort
	}
	order := "DESC"
	if strings.EqualFold(cond.SortType, "asc") {
		order = "ASC"
	}
	limit := cond.Limit
	if limit <= 0 {
		limit = 10
	}

	rows, err := r.db.Query(
		fmt.Sprintf(
			"SELECT id, client_id, member_id, issue_at, jwt FROM jwt_histories WHERE client_id = ? AND deleted_at IS NULL ORDER BY %s %s LIMIT ? OFFSET ?",
			sortCol, order,
		),
		cond.ClientID, limit, cond.Offset,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []*domclient.JwtHistory
	for rows.Next() {
		h := &domclient.JwtHistory{}
		if err := rows.Scan(&h.ID, &h.ClientID, &h.MemberID, &h.IssueAt, &h.Jwt); err != nil {
			return nil, err
		}
		list = append(list, h)
	}
	return list, rows.Err()
}

func (r *SQLJwtHistoryRepository) Save(clientID uint64, memberID string, issueAt time.Time, jwt string) error {
	now := time.Now()
	_, err := r.db.Exec(
		"INSERT INTO jwt_histories (client_id, member_id, issue_at, jwt, created_at, created_by, updated_at, updated_by, version) VALUES (?, ?, ?, ?, ?, 0, ?, 0, 1)",
		clientID, memberID, issueAt, jwt, now, now,
	)
	return err
}
