/*
 * JWT履歴リポジトリの Exposed 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

import com.authorization.domain.client.JwtHistory
import com.authorization.domain.client.JwtHistoryCondition
import com.authorization.domain.client.JwtHistoryRepository
import com.authorization.infrastructure.model.JwtHistories
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

private val ALLOWED_SORT = setOf("issue_at", "member_id")

/**
 * Exposed を使用した JWT 履歴リポジトリの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class ExposedJwtHistoryRepository(private val db: Database) : JwtHistoryRepository {

    /**
     * 検索条件に一致する JWT 履歴の総件数を返します。
     *
     * @param cond 検索条件
     * @return 総件数
     */
    override suspend fun countByCondition(cond: JwtHistoryCondition): Int = newSuspendedTransaction(db = db) {
        JwtHistories.selectAll()
            .where { (JwtHistories.clientId eq cond.clientId) and JwtHistories.deletedAt.isNull() }
            .count().toInt()
    }

    /**
     * 検索条件に一致する JWT 履歴一覧を取得します。
     *
     * @param cond 検索条件
     * @return JWT 履歴一覧
     */
    override suspend fun findByCondition(cond: JwtHistoryCondition): List<JwtHistory> = newSuspendedTransaction(db = db) {
        val sortCol = if (ALLOWED_SORT.contains(cond.sort)) cond.sort else "issue_at"
        val sortOrder = if (cond.sortType.equals("asc", ignoreCase = true)) SortOrder.ASC else SortOrder.DESC
        val col: org.jetbrains.exposed.sql.Expression<*> = when (sortCol) {
            "member_id" -> JwtHistories.memberId
            else        -> JwtHistories.issueAt
        }

        JwtHistories.selectAll()
            .where { (JwtHistories.clientId eq cond.clientId) and JwtHistories.deletedAt.isNull() }
            .orderBy(col to sortOrder)
            .limit(maxOf(1, cond.limit)).offset(cond.offset.toLong())
            .map { row ->
                JwtHistory(
                    id        = row[JwtHistories.id].value,
                    clientId  = row[JwtHistories.clientId],
                    memberId  = row[JwtHistories.memberId],
                    issueAt   = row[JwtHistories.issueAt],
                    jwt       = row[JwtHistories.jwt],
                    createdAt = row[JwtHistories.createdAt],
                    deletedAt = row[JwtHistories.deletedAt],
                )
            }
    }

    /**
     * JWT 履歴を保存します。
     *
     * @param clientId クライアント ID
     * @param memberId メンバー ID
     * @param issueAt 発行日時
     * @param jwt JWT 文字列
     */
    override suspend fun save(clientId: Long, memberId: String, issueAt: LocalDateTime, jwt: String) =
        newSuspendedTransaction(db = db) {
            val now = LocalDateTime.now()
            JwtHistories.insert {
                it[JwtHistories.clientId]  = clientId
                it[JwtHistories.memberId]  = memberId
                it[JwtHistories.issueAt]   = issueAt
                it[JwtHistories.jwt]       = jwt
                it[JwtHistories.createdAt] = now
                it[JwtHistories.createdBy] = 0
                it[JwtHistories.updatedAt] = now
                it[JwtHistories.updatedBy] = 0
                it[JwtHistories.version]   = 1
            }
            Unit
        }
}
