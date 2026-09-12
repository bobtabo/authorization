/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.cache;

import com.authorization.config.AppConfig;
import com.authorization.domain.invitation.repositories.InvitationAuthRepository;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Redis を使用した招待認証キャッシュRepositoryの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class RedisInvitationAuthRepository implements InvitationAuthRepository {

    private final JedisPool pool;
    private final String cachePrefix;

    /**
     * コンストラクタ。
     *
     * @param pool Redis接続プール
     * @param cfg アプリケーション設定
     */
    public RedisInvitationAuthRepository(JedisPool pool, AppConfig cfg) {
        this.pool = pool;
        this.cachePrefix = cfg.app().cachePrefix();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(String token, int role, int ttl) {
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(cacheKey(token), ttl, String.valueOf(role));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer find(String token) {
        try (Jedis jedis = pool.getResource()) {
            String v = jedis.get(cacheKey(token));
            if (v == null) {
                return null;
            }
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer consume(String token) {
        try (Jedis jedis = pool.getResource()) {
            String v = jedis.getDel(cacheKey(token));
            if (v == null) {
                return null;
            }
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String token) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(cacheKey(token));
        }
    }

    /**
     * キャッシュキーを組み立てます。
     *
     * @param token 招待トークン
     * @return キャッシュキー
     */
    private String cacheKey(String token) {
        return cachePrefix + ":invitation_auth:invitation_auth:" + token;
    }
}
