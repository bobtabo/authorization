package cache

import (
	"authorization-go/internal/config"
	"context"
	"time"

	"github.com/redis/go-redis/v9"
)

// RedisInvitationAuthRepository は domain/invitation.AuthRepository の Redis 実装です。
type RedisInvitationAuthRepository struct {
	rdb *redis.Client
	cfg *config.Config
}

// NewRedisInvitationAuthRepository は RedisInvitationAuthRepository を生成します。
func NewRedisInvitationAuthRepository(rdb *redis.Client, cfg *config.Config) *RedisInvitationAuthRepository {
	return &RedisInvitationAuthRepository{rdb: rdb, cfg: cfg}
}

// Store はトークンを指定秒数 Redis にキャッシュします。
func (r *RedisInvitationAuthRepository) Store(token string, ttl int) error {
	ctx := context.Background()
	return r.rdb.Set(ctx, r.key(token), token, time.Duration(ttl)*time.Second).Err()
}

// Find はキャッシュからトークンを取得します。存在しない場合は空文字を返します。
func (r *RedisInvitationAuthRepository) Find(token string) (string, error) {
	ctx := context.Background()
	val, err := r.rdb.Get(ctx, r.key(token)).Result()
	if err == redis.Nil {
		return "", nil
	}
	return val, err
}

// Remove はキャッシュからトークンを削除します。
func (r *RedisInvitationAuthRepository) Remove(token string) error {
	ctx := context.Background()
	return r.rdb.Del(ctx, r.key(token)).Err()
}

func (r *RedisInvitationAuthRepository) key(token string) string {
	prefix := r.cfg.App.CachePrefix
	if prefix == "" {
		return "invitation_auth:invitation_auth:" + token
	}
	return prefix + ":invitation_auth:invitation_auth:" + token
}
