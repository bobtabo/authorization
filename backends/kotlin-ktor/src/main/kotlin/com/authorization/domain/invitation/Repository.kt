/*
 * 招待ドメインリポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.invitation

/**
 * 招待リポジトリのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
interface Repository {

    /**
     * 現在有効な招待情報を取得します。
     *
     * @return 招待 VO、または null
     */
    suspend fun getCurrent(): Vo?

    /**
     * 招待トークンを新規発行します。
     *
     * @return 発行された招待 VO
     */
    suspend fun issue(): Vo

    /**
     * 招待トークンに一致する招待情報を取得します。
     *
     * @param token 招待トークン
     * @return 招待 VO、または null
     */
    suspend fun findByToken(token: String): Vo?
}
