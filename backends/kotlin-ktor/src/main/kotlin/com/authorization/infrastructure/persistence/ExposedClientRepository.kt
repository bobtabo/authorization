/*
 * クライアントリポジトリの Exposed 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

import com.authorization.domain.client.Client
import com.authorization.domain.client.Condition
import com.authorization.domain.client.Repository
import org.jetbrains.exposed.sql.Database

/**
 * Exposed を使用したクライアントリポジトリの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class ExposedClientRepository(private val db: Database) : Repository {

    /**
     * 検索条件に一致するクライアント一覧を取得します。
     *
     * @param cond 検索条件
     * @return クライアント一覧
     */
    override suspend fun findByCondition(cond: Condition): List<Client> = TODO()

    /**
     * 指定した ID のクライアントを取得します。
     *
     * @param id クライアント ID
     * @return クライアント、または null
     */
    override suspend fun findById(id: Long): Client? = TODO()

    /**
     * アクセストークンに一致するクライアントを取得します。
     *
     * @param token アクセストークン
     * @return クライアント、または null
     */
    override suspend fun findByAccessToken(token: String): Client? = TODO()

    /**
     * 識別子に一致するクライアントを取得します。
     *
     * @param identifier 識別子
     * @return クライアント、または null
     */
    override suspend fun findByIdentifier(identifier: String): Client? = TODO()

    /**
     * クライアントを保存します（新規登録・更新）。
     *
     * @param c クライアントエンティティ
     * @return 保存後のクライアントエンティティ
     */
    override suspend fun save(c: Client): Client = TODO()

    /**
     * クライアントを論理削除します。
     *
     * @param id クライアント ID
     * @param deletedBy 削除者スタッフ ID
     */
    override suspend fun softDelete(id: Long, deletedBy: Long) = TODO()
}
