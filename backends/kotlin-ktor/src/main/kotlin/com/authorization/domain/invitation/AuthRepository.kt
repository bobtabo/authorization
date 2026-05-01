/*
 * 招待認証リポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.invitation

/**
 * 招待認証トークンのキャッシュリポジトリインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
interface AuthRepository {
    /** トークンを指定秒数キャッシュします。 */
    suspend fun store(token: String, ttl: Long)
    /** トークンを取得します。存在しない場合は null を返します。 */
    suspend fun find(token: String): String?
    /** トークンを削除します。 */
    suspend fun remove(token: String)
}
