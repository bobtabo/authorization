/*
 * ゲートドメインの値オブジェクトモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.gate

/**
 * JWT 発行結果の値オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class IssueVo(val token: String)

/**
 * JWT 検証結果の値オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class VerifyVo(val claims: Map<String, Any?>)

/**
 * ゲートキャッシュリポジトリのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
interface CacheRepository {

    /**
     * キャッシュから JWT を取得します。
     *
     * @param identifier クライアント識別子
     * @param memberId メンバー ID
     * @return JWT、またはキャッシュがなければ null
     */
    suspend fun getJwt(identifier: String, memberId: String): String?

    /**
     * JWT をキャッシュに保存します。
     *
     * @param identifier クライアント識別子
     * @param memberId メンバー ID
     * @param token JWT
     * @param ttl キャッシュ有効期間（秒）
     */
    suspend fun putJwt(identifier: String, memberId: String, token: String, ttl: Long)
}
