/*
 * クライアントドメインリポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.client

import java.time.LocalDateTime

/**
 * クライアントリポジトリのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
interface Repository {

    /**
     * 検索条件に一致するクライアント一覧を取得します。
     *
     * @param cond 検索条件
     * @return クライアント一覧
     */
    suspend fun findByCondition(cond: Condition): List<Client>

    /**
     * 指定した ID のクライアントを取得します。
     *
     * @param id クライアント ID
     * @return クライアント、または null
     */
    suspend fun findById(id: Long): Client?

    /**
     * アクセストークンに一致するクライアントを取得します。
     *
     * @param token アクセストークン
     * @return クライアント、または null
     */
    suspend fun findByAccessToken(token: String): Client?

    /**
     * 識別子に一致するクライアントを取得します。
     *
     * @param identifier 識別子
     * @return クライアント、または null
     */
    suspend fun findByIdentifier(identifier: String): Client?

    /**
     * クライアントを保存します（新規登録・更新）。
     *
     * @param c クライアントエンティティ
     * @return 保存後のクライアントエンティティ
     */
    suspend fun save(c: Client): Client

    /**
     * クライアントを論理削除します。
     *
     * @param id クライアント ID
     * @param deletedBy 削除者スタッフ ID
     * @param version 楽観排他ロック用バージョン
     */
    suspend fun softDelete(id: Long, deletedBy: Long, version: Int)
}

/**
 * JWT履歴リポジトリのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
interface JwtHistoryRepository {

    /**
     * 指定したクライアント ID の JWT 履歴一覧を issue_at 降順で取得します。
     *
     * @param clientId クライアント ID
     * @return JWT 履歴一覧
     */
    suspend fun findByClientId(clientId: Long): List<JwtHistory>

    /**
     * JWT 履歴を保存します。
     *
     * @param clientId クライアント ID
     * @param memberId メンバー ID
     * @param issueAt 発行日時
     * @param jwt JWT 文字列
     */
    suspend fun save(clientId: Long, memberId: String, issueAt: LocalDateTime, jwt: String)
}
