package db

import (
	"authorization-go-echo/internal/config"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// New はデータベース接続を生成して返します。
func New(cfg *config.Config) (*gorm.DB, error) {
	logLevel := logger.Warn
	if cfg.App.Env == "local" {
		logLevel = logger.Info
	}

	return gorm.Open(mysql.Open(cfg.DB.DSN), &gorm.Config{
		Logger: logger.Default.LogMode(logLevel),
	})
}
