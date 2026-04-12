package cache

import (
	"authorization-go/internal/config"

	"github.com/redis/go-redis/v9"
)

func New(cfg *config.Config) *redis.Client {
	return redis.NewClient(&redis.Options{
		Addr:     cfg.Redis.Addr,
		Password: cfg.Redis.Password,
		DB:       cfg.Redis.DB,
	})
}
