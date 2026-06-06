/*
 * クライアントリポジトリの Exposed 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

import com.authorization.domain.client.Client
import com.authorization.domain.client.ClientStatus
import com.authorization.domain.client.Condition
import com.authorization.domain.client.Repository
import com.authorization.infrastructure.model.Clients
import com.authorization.support.AppException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

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
    private fun applyFilters(cond: Condition): org.jetbrains.exposed.sql.Query {
        var query = Clients.selectAll()
        cond.keyword?.let { kw ->
            query = query.andWhere { (Clients.name like "%$kw%") or (Clients.email like "%$kw%") }
        }
        cond.startFrom?.let { from ->
            query = query.andWhere { Clients.startAt greaterEq from }
        }
        cond.startTo?.let { to ->
            query = query.andWhere { Clients.startAt lessEq to }
        }
        if (cond.statuses.isNotEmpty()) {
            query = query.andWhere { Clients.status inList cond.statuses }
        }
        return query
    }

    override suspend fun findByCondition(cond: Condition): List<Client> = newSuspendedTransaction(db = db) {
        var query = applyFilters(cond)

        val sortColumn = when (cond.sort) {
            "name" -> Clients.name
            "status" -> Clients.status
            "created_at" -> Clients.createdAt
            "updated_at" -> Clients.updatedAt
            "start_at" -> Clients.startAt
            else -> Clients.createdAt
        }
        val sortOrder = if (cond.sortType == "asc") SortOrder.ASC else SortOrder.DESC
        query = query.orderBy(sortColumn to sortOrder)

        if (cond.limit > 0) {
            query = query.limit(cond.limit).offset(cond.offset.toLong())
        }

        query.map { rowToClient(it) }
    }

    override suspend fun countByCondition(cond: Condition): Int = newSuspendedTransaction(db = db) {
        applyFilters(cond).count().toInt()
    }

    /**
     * 指定した ID のクライアントを取得します。
     *
     * @param id クライアント ID
     * @return クライアント、または null
     */
    override suspend fun findById(id: Long): Client? = newSuspendedTransaction(db = db) {
        Clients.selectAll().where { Clients.id eq id }.firstOrNull()?.let { rowToClient(it) }
    }

    /**
     * アクセストークンに一致するクライアントを取得します。
     *
     * @param token アクセストークン
     * @return クライアント、または null
     */
    override suspend fun findByAccessToken(token: String): Client? = newSuspendedTransaction(db = db) {
        Clients.selectAll().where {
            (Clients.accessToken eq token) and
            (Clients.status eq ClientStatus.ACTIVE) and
            Clients.deletedAt.isNull()
        }.firstOrNull()?.let { rowToClient(it) }
    }

    /**
     * 識別子に一致するクライアントを取得します。
     *
     * @param identifier 識別子
     * @return クライアント、または null
     */
    override suspend fun findByIdentifier(identifier: String): Client? = newSuspendedTransaction(db = db) {
        Clients.selectAll().where { Clients.identifier eq identifier }
            .firstOrNull()?.let { rowToClient(it) }
    }

    /**
     * クライアントを保存します（新規登録・更新）。
     *
     * @param c クライアントエンティティ
     * @return 保存後のクライアントエンティティ
     */
    override suspend fun save(c: Client): Client = newSuspendedTransaction(db = db) {
        if (c.id == 0L) {
            val newId = Clients.insertAndGetId {
                it[name]        = c.name
                it[identifier]  = c.identifier
                it[postCode]    = c.postCode
                it[pref]        = c.pref
                it[city]        = c.city
                it[address]     = c.address
                it[building]    = c.building
                it[tel]         = c.tel
                it[email]       = c.email
                it[accessToken] = c.accessToken
                it[privateKey]  = c.privateKey
                it[publicKey]   = c.publicKey
                it[fingerprint] = c.fingerprint
                it[status]      = c.status
                it[startAt]     = c.startAt
                it[stopAt]      = c.stopAt
                it[createdAt]   = c.createdAt
                it[createdBy]   = c.createdBy?.toInt() ?: 0
                it[updatedAt]   = c.updatedAt
                it[updatedBy]   = c.updatedBy?.toInt() ?: 0
                it[version]     = c.version
            }
            c.copy(id = newId.value)
        } else {
            val currentVersion = Clients.selectAll()
                .where { Clients.id eq c.id }
                .firstOrNull()
                ?.get(Clients.version)
                ?: throw AppException.conflict()
            if (currentVersion != c.version) throw AppException.conflict()
            Clients.update({ Clients.id eq c.id }) {
                it[name]      = c.name
                it[postCode]  = c.postCode
                it[pref]      = c.pref
                it[city]      = c.city
                it[address]   = c.address
                it[building]  = c.building
                it[tel]       = c.tel
                it[email]     = c.email
                it[status]    = c.status
                it[startAt]   = c.startAt
                it[stopAt]    = c.stopAt
                it[updatedAt] = c.updatedAt
                it[updatedBy] = c.updatedBy?.toInt() ?: 0
                it[version]   = c.version + 1
            }
            c.copy(version = c.version + 1)
        }
    }

    /**
     * クライアントを論理削除します。
     *
     * @param id クライアント ID
     * @param deletedBy 削除者スタッフ ID
     * @param version 楽観排他ロック用バージョン
     */
    override suspend fun softDelete(id: Long, deletedBy: Long, version: Int) = newSuspendedTransaction(db = db) {
        val currentVersion = Clients.selectAll()
            .where { Clients.id eq id }
            .firstOrNull()
            ?.get(Clients.version)
            ?: throw AppException.conflict()
        if (currentVersion != version) throw AppException.conflict()
        val now = LocalDateTime.now()
        Clients.update({ Clients.id eq id }) {
            it[deletedAt]         = now
            it[Clients.deletedBy] = deletedBy.toInt()
            it[updatedAt]         = now
            it[updatedBy]         = deletedBy.toInt()
        }
        Unit
    }

    private fun rowToClient(row: ResultRow) = Client(
        id          = row[Clients.id].value,
        name        = row[Clients.name],
        identifier  = row[Clients.identifier],
        postCode    = row[Clients.postCode],
        pref        = row[Clients.pref],
        city        = row[Clients.city],
        address     = row[Clients.address],
        building    = row[Clients.building] ?: "",
        tel         = row[Clients.tel],
        email       = row[Clients.email],
        accessToken = row[Clients.accessToken],
        privateKey  = row[Clients.privateKey],
        publicKey   = row[Clients.publicKey],
        fingerprint = row[Clients.fingerprint],
        status      = row[Clients.status],
        startAt     = row[Clients.startAt],
        stopAt      = row[Clients.stopAt],
        createdAt   = row[Clients.createdAt],
        createdBy   = row[Clients.createdBy]?.toLong(),
        updatedAt   = row[Clients.updatedAt],
        updatedBy   = row[Clients.updatedBy]?.toLong(),
        deletedAt   = row[Clients.deletedAt],
        deletedBy   = row[Clients.deletedBy]?.toLong(),
        version     = row[Clients.version],
    )
}
