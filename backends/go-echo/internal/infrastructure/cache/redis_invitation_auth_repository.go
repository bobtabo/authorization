package cache

import (
	"authorization-go-echo/internal/config"
	"context"
	"time"

	"github.com/redis/go-redis/v9"
)

type RedisInvitationAuthRepository struct {
	rdb *redis.Client
	cfg *config.Config
}

func NewRedisInvitationAuthRepository(rdb *redis.Client, cfg *config.Config) *RedisInvitationAuthRepository {
	return &RedisInvitationAuthRepository{rdb: rdb, cfg: cfg}
}

func (r *RedisInvitationAuthRepository) Store(token string, ttl int) error {
	ctx := context.Background()
	return r.rdb.Set(ctx, r.key(token), token, time.Duration(ttl)*time.Second).Err()
}

func (r *RedisInvitationAuthRepository) Find(token string) (string, error) {
	ctx := context.Background()
	val, err := r.rdb.Get(ctx, r.key(token)).Result()
	if err == redis.Nil {
		return "", nil
	}
	return val, err
}

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
