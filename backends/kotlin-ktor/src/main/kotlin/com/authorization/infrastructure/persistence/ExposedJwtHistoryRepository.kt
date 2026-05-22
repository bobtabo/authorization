/*
 * JWT履歴リポジトリの Exposed 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

import com.authorization.domain.client.JwtHistory
import com.authorization.domain.client.JwtHistoryRepository
import com.authorization.infrastructure.model.JwtHistories
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

/**
 * Exposed を使用した JWT 履歴リポジトリの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class ExposedJwtHistoryRepository(private val db: Database) : JwtHistoryRepository {

    /**
     * 指定したクライアント ID の JWT 履歴一覧を issue_at 降順で取得します。
     *
     * @param clientId クライアント ID
     * @return JWT 履歴一覧
     */
    override suspend fun findByClientId(clientId: Long): List<JwtHistory> = newSuspendedTransaction(db = db) {
        JwtHistories.selectAll()
            .where {
                (JwtHistories.clientId eq clientId) and
                JwtHistories.deletedAt.isNull()
            }
            .orderBy(JwtHistories.issueAt to SortOrder.DESC)
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
