package cache

import (
	"authorization-go/internal/config"
	"context"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
)

type RedisGateRepository struct {
	rdb *redis.Client
	cfg *config.Config
}

func NewRedisGateRepository(rdb *redis.Client, cfg *config.Config) *RedisGateRepository {
	return &RedisGateRepository{rdb: rdb, cfg: cfg}
}

func (r *RedisGateRepository) GetJwt(identifier, memberID string) (string, error) {
	ctx := context.Background()
	val, err := r.rdb.Get(ctx, r.key(identifier, memberID)).Result()
	if err == redis.Nil {
		return "", nil
	}
	return val, err
}

func (r *RedisGateRepository) PutJwt(identifier, memberID, token string, ttl int) error {
	ctx := context.Background()
	return r.rdb.Set(ctx, r.key(identifier, memberID), token, time.Duration(ttl)*time.Second).Err()
}

func (r *RedisGateRepository) key(identifier, memberID string) string {
	prefix := r.cfg.App.CachePrefix
	if prefix == "" {
		return fmt.Sprintf("gate.jwt:%s:%s", identifier, memberID)
	}
	return fmt.Sprintf("%s:gate.jwt:%s:%s", prefix, identifier, memberID)
}
