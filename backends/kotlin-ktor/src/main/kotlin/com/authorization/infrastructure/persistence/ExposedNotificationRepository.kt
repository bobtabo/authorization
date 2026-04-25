/*
 * 通知リポジトリの Exposed 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

import com.authorization.domain.notification.Notification
import com.authorization.domain.notification.Page
import com.authorization.domain.notification.Repository
import com.authorization.infrastructure.model.Notifications
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Base64

/**
 * Exposed を使用した通知リポジトリの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class ExposedNotificationRepository(private val db: Database) : Repository {

    /**
     * 通知をページング取得します。
     *
     * @param staffId スタッフ ID
     * @param cursor ページカーソル
     * @param limit 取得件数
     * @return 通知ページ
     */
    override suspend fun listPage(staffId: Long, cursor: String?, limit: Int): Page = newSuspendedTransaction(db = db) {
        var query = Notifications.selectAll()
            .where {
                (Notifications.staffId eq staffId) and Notifications.deletedAt.isNull()
            }

        if (cursor != null) {
            val (dt, cursorId) = decodeCursor(cursor) ?: error("invalid_cursor")
            query = query.andWhere {
                (Notifications.createdAt less dt) or
                ((Notifications.createdAt eq dt) and (Notifications.id less cursorId))
            }
        }

        query = query.orderBy(
            Notifications.createdAt to SortOrder.DESC,
            Notifications.id to SortOrder.DESC,
        ).limit(limit + 1)

        val rows    = query.toList()
        val hasNext = rows.size > limit
        val items   = rows.take(limit).map { rowToNotification(it) }
        val nextCursor = if (hasNext) items.lastOrNull()?.let { encodeCursor(it.createdAt, it.id) } else null

        Page(items = items, nextCursor = nextCursor)
    }

    /**
     * 通知の未読件数と総件数を取得します。
     *
     * @param staffId スタッフ ID
     * @return （未読件数, 総件数）のペア
     */
    override suspend fun counts(staffId: Long): Pair<Long, Long> = newSuspendedTransaction(db = db) {
        val total = Notifications.selectAll()
            .where { (Notifications.staffId eq staffId) and Notifications.deletedAt.isNull() }
            .count()
        val unread = Notifications.selectAll()
            .where {
                (Notifications.staffId eq staffId) and
                (Notifications.read eq false) and
                Notifications.deletedAt.isNull()
            }
            .count()
        Pair(unread, total)
    }

    /**
     * スタッフの通知を一括既読にします。
     *
     * @param staffId スタッフ ID
     * @param ids 対象通知 ID リスト
     * @param all すべて既読にする場合は true
     * @return 更新件数
     */
    override suspend fun bulkMarkRead(staffId: Long, ids: List<Long>, all: Boolean): Long = newSuspendedTransaction(db = db) {
        val now = LocalDateTime.now()
        if (all) {
            Notifications.update({
                (Notifications.staffId eq staffId) and
                (Notifications.read eq false) and
                Notifications.deletedAt.isNull()
            }) {
                it[Notifications.read]      = true
                it[Notifications.updatedAt] = now
            }.toLong()
        } else {
            if (ids.isEmpty()) return@newSuspendedTransaction 0L
            Notifications.update({
                (Notifications.staffId eq staffId) and
                (Notifications.id inList ids) and
                (Notifications.read eq false) and
                Notifications.deletedAt.isNull()
            }) {
                it[Notifications.read]      = true
                it[Notifications.updatedAt] = now
            }.toLong()
        }
    }

    /**
     * 通知を保存します。
     *
     * @param staffId 宛先スタッフ ID
     * @param messageType メッセージ種別
     * @param title タイトル
     * @param message 本文
     * @param createdBy 作成者スタッフ ID
     * @param url 関連 URL
     */
    override suspend fun store(staffId: Long, messageType: Int, title: String, message: String, createdBy: Long, url: String?) = newSuspendedTransaction(db = db) {
        val now = LocalDateTime.now()
        Notifications.insert {
            it[Notifications.staffId]     = staffId
            it[Notifications.messageType] = messageType
            it[Notifications.title]       = title
            it[Notifications.message]     = message
            it[Notifications.url]         = url
            it[Notifications.read]        = false
            it[Notifications.createdAt]   = now
            it[Notifications.createdBy]   = createdBy.toInt()
            it[Notifications.updatedAt]   = now
            it[Notifications.updatedBy]   = createdBy.toInt()
            it[Notifications.version]     = 1
        }
        Unit
    }

    /**
     * 通知の属性を部分更新します。
     *
     * @param id 通知 ID
     * @param attrs 更新する属性マップ
     * @return 更新成功なら true
     */
    override suspend fun patch(id: Long, attrs: Map<String, Any?>): Boolean = newSuspendedTransaction(db = db) {
        val now = LocalDateTime.now()
        val count = Notifications.update({ Notifications.id eq id }) {
            for ((key, value) in attrs) {
                when (key) {
                    "read" -> it[Notifications.read] = value as Boolean
                }
            }
            it[Notifications.updatedAt] = now
        }
        count > 0
    }

    private fun rowToNotification(row: ResultRow) = Notification(
        id          = row[Notifications.id].value,
        staffId     = row[Notifications.staffId],
        messageType = row[Notifications.messageType],
        title       = row[Notifications.title],
        message     = row[Notifications.message],
        url         = row[Notifications.url],
        read        = row[Notifications.read],
        createdAt   = row[Notifications.createdAt],
        createdBy   = row[Notifications.createdBy].toLong(),
        updatedAt   = row[Notifications.updatedAt],
        updatedBy   = row[Notifications.updatedBy].toLong(),
        deletedAt   = row[Notifications.deletedAt],
        version     = row[Notifications.version],
    )

    private fun encodeCursor(dt: LocalDateTime, id: Long): String {
        val epochSec = dt.toEpochSecond(ZoneOffset.UTC)
        return Base64.getEncoder().encodeToString("$epochSec,$id".toByteArray())
    }

    private fun decodeCursor(cursor: String): Pair<LocalDateTime, Long>? = runCatching {
        val raw   = String(Base64.getDecoder().decode(cursor))
        val parts = raw.split(",", limit = 2)
        val dt    = LocalDateTime.ofEpochSecond(parts[0].toLong(), 0, ZoneOffset.UTC)
        dt to parts[1].toLong()
    }.getOrNull()
}
