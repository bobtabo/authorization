// Package db はデータベース接続の初期化を提供します。
package db

import (
	"authorization-go/internal/config"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// New は設定を元に GORM DB インスタンスを生成します。
//
// cfg: アプリケーション設定
// 戻り値: GORM DB インスタンス、またはエラー
func New(cfg *config.Config) (*gorm.DB, error) {
	logLevel := logger.Warn
	if cfg.App.Env == "local" {
		logLevel = logger.Info
	}

	return gorm.Open(mysql.Open(cfg.DB.DSN), &gorm.Config{
		Logger: logger.Default.LogMode(logLevel),
	})
}
