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
     * 指定ロールの現在有効な招待情報を取得します。
     *
     * @param role ロール（1=管理者、2=メンバー）
     * @return 招待 VO、または null
     */
    suspend fun getCurrentByRole(role: Int): Vo?

    /**
     * 指定ロールで招待トークンを新規発行します。
     *
     * @param role ロール（1=管理者、2=メンバー）
     * @return 発行された招待 VO
     */
    suspend fun issue(role: Int): Vo

    /**
     * 招待トークンに一致する招待情報を取得します。
     *
     * @param token 招待トークン
     * @return 招待 VO、または null
     */
    suspend fun findByToken(token: String): Vo?
}
