package com.authorization.infrastructure.persistence

import com.authorization.domain.notification.Notification
import com.authorization.domain.notification.Page
import com.authorization.domain.notification.Repository
import org.jetbrains.exposed.sql.Database

class ExposedNotificationRepository(private val db: Database) : Repository {
    override suspend fun listPage(staffId: Long, cursor: String?, limit: Int): Page = TODO()
    override suspend fun counts(staffId: Long): Pair<Long, Long> = TODO()
    override suspend fun bulkMarkRead(staffId: Long, ids: List<Long>, all: Boolean): Long = TODO()
    override suspend fun store(staffId: Long, messageType: Int, title: String, message: String, createdBy: Long, url: String?) = TODO()
    override suspend fun patch(id: Long, attrs: Map<String, Any?>): Boolean = TODO()
}
