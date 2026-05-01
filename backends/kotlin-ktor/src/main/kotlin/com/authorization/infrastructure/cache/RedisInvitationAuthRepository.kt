/*
 * 招待認証キャッシュリポジトリの Redis 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.cache

import com.authorization.config.Config
import com.authorization.domain.invitation.AuthRepository
import redis.clients.jedis.JedisPool

/**
 * Redis を使用した招待認証キャッシュリポジトリの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class RedisInvitationAuthRepository(
    private val pool: JedisPool,
    private val cfg: Config,
) : AuthRepository {

    override suspend fun store(token: String, ttl: Long) {
        pool.resource.use { it.setex(cacheKey(token), ttl, token) }
    }

    override suspend fun find(token: String): String? =
        pool.resource.use { it.get(cacheKey(token)) }

    override suspend fun remove(token: String) {
        pool.resource.use { it.del(cacheKey(token)) }
    }

    private fun cacheKey(token: String): String =
        "${cfg.app.cachePrefix}:invitation_auth:invitation_auth:$token"
}
