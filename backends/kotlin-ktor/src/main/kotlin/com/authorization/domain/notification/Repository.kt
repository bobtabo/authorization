package com.authorization.domain.notification

interface Repository {
    suspend fun listPage(staffId: Long, cursor: String?, limit: Int): Page
    suspend fun counts(staffId: Long): Pair<Long, Long>
    suspend fun bulkMarkRead(staffId: Long, ids: List<Long>, all: Boolean): Long
    suspend fun store(staffId: Long, messageType: Int, title: String, message: String, createdBy: Long, url: String? = null)
    suspend fun patch(id: Long, attrs: Map<String, Any?>): Boolean
}
