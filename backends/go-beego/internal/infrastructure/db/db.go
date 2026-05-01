package db

import (
	"authorization-go-beego/internal/config"
	"authorization-go-beego/internal/infrastructure/model"
	"sync"

	"github.com/beego/beego/v2/client/orm"
	_ "github.com/go-sql-driver/mysql"
)

var (
	registerOnce sync.Once
	registerErr  error
)

// New はデータベース接続を生成して返します。
func New(cfg *config.Config) (orm.Ormer, error) {
	registerOnce.Do(func() {
		if err := orm.RegisterDataBase("default", "mysql", cfg.DB.DSN); err != nil {
			registerErr = err
			return
		}
		orm.RegisterModel(
			new(model.Client),
			new(model.Staff),
			new(model.Invitation),
			new(model.Notification),
		)
	})
	if registerErr != nil {
		return nil, registerErr
	}
	return orm.NewOrm(), nil
}
