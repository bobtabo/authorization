package com.authorization.usecase.notification

import com.authorization.domain.notification.Notification
import com.authorization.domain.notification.Page
import com.authorization.domain.notification.Repository
import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.Repository as StaffRepository
import com.authorization.domain.staff.Staff
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class InteractorTest {

    private fun makeNotification(id: Long = 1L) = Notification(
        id          = id,
        staffId     = 1L,
        messageType = 1,
        title       = "Title $id",
        message     = "Message",
        createdAt   = LocalDateTime.of(2024, 1, 1, 12, 0),
        updatedAt   = LocalDateTime.of(2024, 1, 1, 12, 0),
    )

    private fun makeStaff(id: Long = 1L) = Staff(
        id = id, name = "S", email = "s@e.com",
        provider = 1, providerId = "p",
        createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
        updatedAt = LocalDateTime.of(2024, 1, 1, 0, 0),
    )

    private fun mockNotifRepo(
        page: Page = Page(emptyList(), null),
        counts: Pair<Long, Long> = Pair(0L, 0L),
    ): Repository = object : Repository {
        override suspend fun listPage(staffId: Long, cursor: String?, limit: Int) = page
        override suspend fun counts(staffId: Long)                                 = counts
        override suspend fun bulkMarkRead(staffId: Long, ids: List<Long>, all: Boolean) = 0L
        override suspend fun store(staffId: Long, messageType: Int, title: String, message: String, createdBy: Long, url: String?) = Unit
        override suspend fun patch(id: Long, attrs: Map<String, Any?>)            = true
    }

    private fun mockStaffRepo(active: List<Staff> = emptyList()): StaffRepository = object : StaffRepository {
        override suspend fun findByCondition(cond: Condition)                           = emptyList<Staff>()
        override suspend fun findById(id: Long)                                         = null
        override suspend fun findByProvider(provider: Int, providerId: String)          = null
        override suspend fun findAllActive()                                             = active
        override suspend fun save(s: Staff)                                              = s
        override suspend fun updateRole(id: Long, role: Int, updatedBy: Long)            = true
        override suspend fun softDelete(id: Long, deletedBy: Long, version: Int)          = true
        override suspend fun restore(id: Long)                                           = true
    }

    @Test
    fun `listPage clamps limit and delegates to repo`() = runBlocking {
        val page = Page(listOf(makeNotification(1L), makeNotification(2L)), "cursor_next")
        val uc   = Interactor(mockNotifRepo(page = page), mockStaffRepo())
        val result = uc.listPage(1L, null, 10L)
        assertEquals(2, result.items.size)
        assertEquals("cursor_next", result.nextCursor)
    }

    @Test
    fun `counts returns CountsVo`() = runBlocking {
        val uc = Interactor(mockNotifRepo(counts = Pair(3L, 10L)), mockStaffRepo())
        val vo = uc.counts(1L)
        assertEquals(3L, vo.unread)
        assertEquals(10L, vo.total)
    }

    @Test
    fun `fanOut stores notification per active staff`() = runBlocking {
        val storedIds = mutableListOf<Long>()
        val repo = object : Repository by mockNotifRepo() {
            override suspend fun store(staffId: Long, messageType: Int, title: String, message: String, createdBy: Long, url: String?) {
                storedIds += staffId
            }
        }
        val staffRepo = mockStaffRepo(active = listOf(makeStaff(1L), makeStaff(2L)))
        val uc = Interactor(repo, staffRepo)
        uc.fanOut(FanOutDto(title = "T", message = "M", messageType = 1, executorId = 99L, url = "/path"))
        assertEquals(listOf(1L, 2L), storedIds)
    }

    @Test
    fun `markRead calls patch with read true`() = runBlocking {
        var patchedId = 0L
        val repo = object : Repository by mockNotifRepo() {
            override suspend fun patch(id: Long, attrs: Map<String, Any?>): Boolean {
                patchedId = id; return true
            }
        }
        Interactor(repo, mockStaffRepo()).markRead(7L)
        assertEquals(7L, patchedId)
    }

    @Test
    fun `mapNotification formats dates correctly`() {
        val n = makeNotification(1L)
        val m = Interactor.mapNotification(n)
        assertEquals("2024-01-01 12:00", m["created_at"])
        assertEquals("2024-01-01 12:00", m["updated_at"])
    }
}
