/*
 * Redis クライアント生成モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.cache

import com.authorization.config.Config
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

/**
 * 設定をもとに Redis 接続プールを生成します。
 *
 * @param cfg アプリケーション設定
 * @return Redis 接続プール
 */
fun newRedisPool(cfg: Config): JedisPool {
    val poolConfig = JedisPoolConfig().apply {
        maxTotal = 16
    }
    return if (cfg.redis.password.isBlank()) {
        JedisPool(poolConfig, cfg.redis.host, cfg.redis.port, 2000, null, cfg.redis.db)
    } else {
        JedisPool(poolConfig, cfg.redis.host, cfg.redis.port, 2000, cfg.redis.password, cfg.redis.db)
    }
}
