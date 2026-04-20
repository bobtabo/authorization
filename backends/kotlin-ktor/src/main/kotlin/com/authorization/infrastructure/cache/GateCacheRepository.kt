package com.authorization.infrastructure.cache

import com.authorization.config.Config
import com.authorization.domain.gate.CacheRepository
import redis.clients.jedis.JedisPool

class GateCacheRepository(
    private val pool: JedisPool,
    private val cfg: Config,
) : CacheRepository {
    override suspend fun getJwt(identifier: String, memberId: String): String? = TODO()
    override suspend fun putJwt(identifier: String, memberId: String, token: String, ttl: Long) = TODO()
}
