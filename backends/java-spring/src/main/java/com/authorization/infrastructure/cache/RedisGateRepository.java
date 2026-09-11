/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.cache;

import com.authorization.config.AppConfig;
import com.authorization.domain.gate.repositories.GateRepository;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Redis を使用したゲートキャッシュRepositoryの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class RedisGateRepository implements GateRepository {

    private final JedisPool pool;
    private final String cachePrefix;

    public RedisGateRepository(JedisPool pool, AppConfig cfg) {
        this.pool = pool;
        this.cachePrefix = cfg.app().cachePrefix();
    }

    @Override
    public void putJwt(String identifier, String memberId, String token, int ttl) {
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(cacheKey(identifier, memberId), ttl, token);
        }
    }

    @Override
    public String getJwt(String identifier, String memberId) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(cacheKey(identifier, memberId));
        }
    }

    /**
     * キャッシュキーを組み立てます。
     *
     * @param identifier クライアント識別子
     * @param memberId メンバーID
     * @return キャッシュキー
     */
    private String cacheKey(String identifier, String memberId) {
        return cachePrefix + ":gate.jwt:" + identifier + ":" + memberId;
    }
}
