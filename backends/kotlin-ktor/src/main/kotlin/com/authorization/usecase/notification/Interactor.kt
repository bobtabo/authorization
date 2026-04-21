package com.authorization.usecase.notification

import com.authorization.domain.notification.Notification
import com.authorization.domain.notification.Page
import com.authorization.domain.notification.Repository
import com.authorization.domain.staff.Repository as StaffRepository
import java.time.format.DateTimeFormatter

class Interactor(
    private val repo: Repository,
    private val staffRepo: StaffRepository,
) {
    suspend fun listPage(staffId: Long, cursor: String?, limit: Long): Page = TODO()
    suspend fun counts(staffId: Long): Pair<Long, Long> = TODO()
    suspend fun bulkMarkRead(staffId: Long): Long = TODO()
    suspend fun fanOut(dto: FanOutDto): Unit = TODO()
    suspend fun markRead(id: Long): Unit = TODO()

    companion object {
        private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

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
