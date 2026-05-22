package persistence

import (
	domclient "authorization-go-echo/internal/domain/client"
	stdsql "database/sql"
	"time"
)

// SQLJwtHistoryRepository は生 SQL で jwt_histories を操作するリポジトリです。
type SQLJwtHistoryRepository struct {
	db *stdsql.DB
}

func NewSQLJwtHistoryRepository(db *stdsql.DB) *SQLJwtHistoryRepository {
	return &SQLJwtHistoryRepository{db: db}
}

func (r *SQLJwtHistoryRepository) FindByClientID(clientID uint64) ([]*domclient.JwtHistory, error) {
	rows, err := r.db.Query(
		"SELECT id, client_id, member_id, issue_at, jwt FROM jwt_histories WHERE client_id = ? AND deleted_at IS NULL ORDER BY issue_at DESC",
		clientID,
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

func (r *SQLJwtHistoryRepository) Save(h *domclient.JwtHistory) error {
	now := time.Now()
	_, err := r.db.Exec(
		"INSERT INTO jwt_histories (client_id, member_id, issue_at, jwt, created_at, created_by, updated_at, updated_by, version) VALUES (?, ?, ?, ?, ?, 0, ?, 0, 1)",
		h.ClientID, h.MemberID, h.IssueAt, h.Jwt, now, now,
	)
	return err
}
