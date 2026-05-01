package cache

import (
	"authorization-go/internal/config"
	"context"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
)

// RedisGateRepository は domain/gate.CacheRepository の Redis 実装です。
type RedisGateRepository struct {
	rdb *redis.Client
	cfg *config.Config
}

// NewRedisGateRepository は RedisGateRepository を生成します。
//
// rdb: Redis クライアント
// cfg: アプリケーション設定
func NewRedisGateRepository(rdb *redis.Client, cfg *config.Config) *RedisGateRepository {
	return &RedisGateRepository{rdb: rdb, cfg: cfg}
}

// GetJwt はキャッシュからゲート JWT を取得します。キャッシュミスの場合は空文字を返します。
func (r *RedisGateRepository) GetJwt(identifier, memberID string) (string, error) {
	ctx := context.Background()
	val, err := r.rdb.Get(ctx, r.key(identifier, memberID)).Result()
	if err == redis.Nil {
		return "", nil
	}
	return val, err
}

// PutJwt はゲート JWT をキャッシュに保存します。
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
