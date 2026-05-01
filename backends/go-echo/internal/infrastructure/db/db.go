package db

import (
	"authorization-go-echo/ent"
	"authorization-go-echo/internal/config"
	stdsql "database/sql"

	"entgo.io/ent/dialect"
	entsql "entgo.io/ent/dialect/sql"
	_ "github.com/go-sql-driver/mysql"
)

// New opens an ent client and returns the underlying *sql.DB alongside it.
// The caller is responsible for closing the ent.Client (which also closes the sql.DB).
func New(cfg *config.Config) (*ent.Client, *stdsql.DB, error) {
	rawDB, err := stdsql.Open("mysql", cfg.DB.DSN)
	if err != nil {
		return nil, nil, err
	}
	drv := entsql.OpenDB(dialect.MySQL, rawDB)
	client := ent.NewClient(ent.Driver(drv))
	return client, rawDB, nil
}
