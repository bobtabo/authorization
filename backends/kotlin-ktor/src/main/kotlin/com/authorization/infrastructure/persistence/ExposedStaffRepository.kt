/*
 * スタッフリポジトリの Exposed 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.Staff
import com.authorization.infrastructure.model.Staffs
import com.authorization.support.AppException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

/**
 * Exposed を使用したスタッフリポジトリの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class ExposedStaffRepository(private val db: Database) : Repository {

    /**
     * 検索条件に一致するスタッフの総件数を返します。
     *
     * @param cond 検索条件
     * @return 総件数
     */
    override suspend fun countByCondition(cond: Condition): Int = newSuspendedTransaction(db = db) {
        var query = Staffs.selectAll()
        cond.keyword?.let { kw ->
            query = query.andWhere { (Staffs.name like "%$kw%") or (Staffs.email like "%$kw%") }
        }
        if (cond.roles.isNotEmpty()) {
            query = query.andWhere { Staffs.role inList cond.roles }
        }
        query.count().toInt()
    }

    /**
     * 検索条件に一致するスタッフ一覧を取得します。
     *
     * @param cond 検索条件
     * @return スタッフ一覧
     */
    override suspend fun findByCondition(cond: Condition): List<Staff> = newSuspendedTransaction(db = db) {
        var query = Staffs.selectAll()
        cond.keyword?.let { kw ->
            query = query.andWhere { (Staffs.name like "%$kw%") or (Staffs.email like "%$kw%") }
        }
        if (cond.roles.isNotEmpty()) {
            query = query.andWhere { Staffs.role inList cond.roles }
        }

        val sortOrder = if (cond.sortType == "desc") SortOrder.DESC else SortOrder.ASC
        val sortCol: org.jetbrains.exposed.sql.Expression<*> = when (cond.sort) {
            "name"       -> Staffs.name
            "role"       -> Staffs.role
            "created_at" -> Staffs.createdAt
            else         -> Staffs.id
        }
        query = query.orderBy(sortCol to sortOrder)

        query.limit(cond.limit, cond.offset.toLong()).map { rowToStaff(it) }
    }

    /**
     * 指定した ID のスタッフを取得します。
     *
     * @param id スタッフ ID
     * @return スタッフ、または null
     */
    override suspend fun findById(id: Long): Staff? = newSuspendedTransaction(db = db) {
        Staffs.selectAll().where { Staffs.id eq id }.firstOrNull()?.let { rowToStaff(it) }
    }

    /**
     * プロバイダー情報に一致するスタッフを取得します。
     *
     * @param provider プロバイダー種別
     * @param providerId プロバイダー ID
     * @return スタッフ、または null
     */
    override suspend fun findByProvider(provider: Int, providerId: String): Staff? = newSuspendedTransaction(db = db) {
        Staffs.selectAll().where {
            (Staffs.provider eq provider) and (Staffs.providerId eq providerId)
        }.firstOrNull()?.let { rowToStaff(it) }
    }

    /**
     * アクティブなスタッフ全員を取得します。
     *
     * @return スタッフ一覧
     */
    override suspend fun findAllActive(): List<Staff> = newSuspendedTransaction(db = db) {
        Staffs.selectAll()
            .where { Staffs.deletedAt.isNull() }
            .orderBy(Staffs.createdAt to SortOrder.DESC)
            .map { rowToStaff(it) }
    }

    /**
     * スタッフを保存します（新規登録・更新）。
     *
     * @param s スタッフエンティティ
     * @return 保存後のスタッフエンティティ
     */
    override suspend fun save(s: Staff): Staff = newSuspendedTransaction(db = db) {
        if (s.id == 0L) {
            val newId = Staffs.insertAndGetId {
                it[name]        = s.name
                it[email]       = s.email
                it[provider]    = s.provider
                it[providerId]  = s.providerId
                it[avatar]      = s.avatar
                it[role]        = s.role
                it[lastLoginAt] = s.lastLoginAt
                it[createdAt]   = s.createdAt
                it[createdBy]   = s.createdBy?.toInt() ?: 0
                it[updatedAt]   = s.updatedAt
                it[updatedBy]   = s.updatedBy?.toInt() ?: 0
                it[version]     = s.version
            }
            s.copy(id = newId.value)
        } else {
            val currentVersion = Staffs.selectAll()
                .where { Staffs.id eq s.id }
                .firstOrNull()
                ?.get(Staffs.version)
                ?: throw AppException.conflict()
            if (currentVersion != s.version) throw AppException.conflict()
            Staffs.update({ Staffs.id eq s.id }) {
                it[name]        = s.name
                it[email]       = s.email
                it[avatar]      = s.avatar
                it[role]        = s.role
                it[lastLoginAt] = s.lastLoginAt
                it[updatedAt]   = s.updatedAt
                it[updatedBy]   = s.updatedBy?.toInt()
                it[version]     = s.version + 1
            }
            s.copy(version = s.version + 1)
        }
    }

    /**
     * スタッフのロールを更新します。
     *
     * @param id スタッフ ID
     * @param role 新しいロール
     * @param updatedBy 更新者スタッフ ID
     * @return 更新成功なら true
     */
    override suspend fun updateRole(id: Long, role: Int, updatedBy: Long): Boolean = newSuspendedTransaction(db = db) {
        val count = Staffs.update({ Staffs.id eq id }) {
            it[Staffs.role]      = role
            it[Staffs.updatedAt] = LocalDateTime.now()
            it[Staffs.updatedBy] = updatedBy.toInt()
        }
        count > 0
    }

    /**
     * スタッフを論理削除します。
     *
     * @param id スタッフ ID
     * @param deletedBy 削除者スタッフ ID
     * @param version 楽観排他ロック用バージョン
     * @return 削除成功なら true
     */
    override suspend fun softDelete(id: Long, deletedBy: Long, version: Int): Boolean = newSuspendedTransaction(db = db) {
        val currentVersion = Staffs.selectAll()
            .where { Staffs.id eq id }
            .firstOrNull()
            ?.get(Staffs.version)
            ?: throw AppException.conflict()
        if (currentVersion != version) throw AppException.conflict()
        val now = LocalDateTime.now()
        val count = Staffs.update({ Staffs.id eq id }) {
            it[deletedAt]         = now
            it[Staffs.deletedBy]  = deletedBy.toInt()
            it[updatedAt]         = now
            it[updatedBy]         = deletedBy.toInt()
        }
        count > 0
    }

    /**
     * 論理削除されたスタッフを復元します。
     *
     * @param id スタッフ ID
     * @return 復元成功なら true
     */
    override suspend fun restore(id: Long): Boolean = newSuspendedTransaction(db = db) {
        val count = Staffs.update({ Staffs.id eq id }) {
            it[deletedAt] = null
            it[deletedBy] = null
            it[updatedAt] = LocalDateTime.now()
        }
        count > 0
    }

    private fun rowToStaff(row: ResultRow) = Staff(
        id          = row[Staffs.id].value,
        name        = row[Staffs.name],
        email       = row[Staffs.email],
        provider    = row[Staffs.provider],
        providerId  = row[Staffs.providerId],
        avatar      = row[Staffs.avatar],
        role        = row[Staffs.role],
        lastLoginAt = row[Staffs.lastLoginAt],
        createdAt   = row[Staffs.createdAt],
        createdBy   = row[Staffs.createdBy]?.toLong(),
        updatedAt   = row[Staffs.updatedAt],
        updatedBy   = row[Staffs.updatedBy]?.toLong(),
        deletedAt   = row[Staffs.deletedAt],
        deletedBy   = row[Staffs.deletedBy]?.toLong(),
        version     = row[Staffs.version],
    )
}
