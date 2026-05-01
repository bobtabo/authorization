/*
 * 通知ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.notification

import com.authorization.domain.notification.CountsVo
import com.authorization.domain.notification.Notification
import com.authorization.domain.notification.Page
import com.authorization.domain.notification.Repository
import com.authorization.domain.staff.Repository as StaffRepository
import java.time.format.DateTimeFormatter

/**
 * 通知ユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(
    private val repo: Repository,
    private val staffRepo: StaffRepository,
) {

    /**
     * 通知のページングリストを取得します。
     *
     * @param staffId スタッフ ID
     * @param cursor ページカーソル
     * @param limit 取得件数
     * @return 通知ページ VO
     */
    suspend fun listPage(staffId: Long, cursor: String?, limit: Long): Page =
        repo.listPage(staffId, cursor, limit.coerceIn(1, 100).toInt())

    /**
     * 通知の未読件数と総件数を取得します。
     *
     * @param staffId スタッフ ID
     * @return 件数 VO（未読件数・総件数）
     */
    suspend fun counts(staffId: Long): CountsVo {
        val (unread, total) = repo.counts(staffId)
        return CountsVo(unread = unread, total = total)
    }

    /**
     * スタッフの通知をすべて既読にします。
     *
     * @param staffId スタッフ ID
     */
    suspend fun bulkMarkRead(staffId: Long) {
        repo.bulkMarkRead(staffId, emptyList(), true)
    }

    /**
     * 通知をすべてのアクティブスタッフへ一括配信します。
     *
     * @param dto 配信 DTO
     */
    suspend fun fanOut(dto: FanOutDto) {
        val staffs = staffRepo.findAllActive()
        staffs.forEach { s ->
            runCatching {
                repo.store(s.id, dto.messageType, dto.title, dto.message, dto.executorId, dto.url.ifBlank { null })
            }
        }
    }

    /**
     * 指定した通知を既読にします。
     *
     * @param id 通知 ID
     */
    suspend fun markRead(id: Long) {
        repo.patch(id, mapOf("read" to true))
    }

    companion object {
        private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        /**
         * 通知エンティティをレスポンス用 Map に変換します。
         *
         * @param n 通知エンティティ
         * @return レスポンス用マップ
         */
        fun mapNotification(n: Notification): Map<String, Any?> = mapOf(
            "id"           to n.id,
            "staff_id"     to n.staffId,
            "message_type" to n.messageType,
            "title"        to n.title,
            "message"      to n.message,
            "url"          to n.url,
            "read"         to n.read,
            "created_at"   to n.createdAt.format(fmt),
            "updated_at"   to n.updatedAt.format(fmt),
        )
    }
}
