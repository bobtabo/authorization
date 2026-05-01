// Package cache はキャッシュ基盤の実装を提供します。
package cache

import (
	"authorization-go/internal/config"

	"github.com/redis/go-redis/v9"
)

// New は設定を元に Redis クライアントを生成します。
//
// cfg: アプリケーション設定
func New(cfg *config.Config) *redis.Client {
	return redis.NewClient(&redis.Options{
		Addr:     cfg.Redis.Addr,
		Password: cfg.Redis.Password,
		DB:       cfg.Redis.DB,
	})
}
