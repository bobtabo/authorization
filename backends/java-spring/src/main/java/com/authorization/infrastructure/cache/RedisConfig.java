/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.cache;

import com.authorization.config.AppConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis 接続プールの Bean 定義です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Configuration
public class RedisConfig {

    /**
     * 設定をもとに Redis 接続プールを生成します。
     *
     * @param cfg アプリケーション設定
     * @return Redis 接続プール
     */
    @Bean(destroyMethod = "close")
    public JedisPool jedisPool(AppConfig cfg) {
        AppConfig.Redis redis = cfg.redis();
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(16);
        if (redis.password() == null || redis.password().isBlank()) {
            return new JedisPool(poolConfig, redis.host(), redis.port(), 2000, null, redis.db());
        }
        return new JedisPool(poolConfig, redis.host(), redis.port(), 2000, redis.password(), redis.db());
    }
}
