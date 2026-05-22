package persistence

import (
	domclient "authorization-go-beego/internal/domain/client"
	"database/sql"
	"time"
)

// SQLJwtHistoryRepository は生 SQL で jwt_histories を操作するリポジトリです。
type SQLJwtHistoryRepository struct {
	db *sql.DB
}

func NewSQLJwtHistoryRepository(db *sql.DB) *SQLJwtHistoryRepository {
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

func (r *SQLJwtHistoryRepository) Save(clientID uint64, memberID string, issueAt time.Time, jwt string) error {
	now := time.Now()
	_, err := r.db.Exec(
		"INSERT INTO jwt_histories (client_id, member_id, issue_at, jwt, created_at, created_by, updated_at, updated_by, version) VALUES (?, ?, ?, ?, ?, 0, ?, 0, 1)",
		clientID, memberID, issueAt, jwt, now, now,
	)
	return err
}
