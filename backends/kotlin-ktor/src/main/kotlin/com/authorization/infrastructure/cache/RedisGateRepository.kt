/*
 * ゲートキャッシュリポジトリの Redis 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.cache

import com.authorization.config.Config
import com.authorization.domain.gate.CacheRepository
import redis.clients.jedis.JedisPool

/**
 * Redis を使用したゲートキャッシュリポジトリの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class RedisGateRepository(
    private val pool: JedisPool,
    private val cfg: Config,
) : CacheRepository {

    /**
     * キャッシュから JWT を取得します。
     *
     * @param identifier クライアント識別子
     * @param memberId メンバー ID
     * @return JWT、またはキャッシュがなければ null
     */
    override suspend fun getJwt(identifier: String, memberId: String): String? = TODO()

    /**
     * JWT をキャッシュに保存します。
     *
     * @param identifier クライアント識別子
     * @param memberId メンバー ID
     * @param token JWT
     * @param ttl キャッシュ有効期間（秒）
     */
    override suspend fun putJwt(identifier: String, memberId: String, token: String, ttl: Long) = TODO()
}
