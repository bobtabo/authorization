package com.authorization.usecase.staff

import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.Staff
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class InteractorTest {

    private fun makeStaff(id: Long = 1L, deletedAt: LocalDateTime? = null) = Staff(
        id         = id,
        name       = "Staff $id",
        email      = "staff$id@example.com",
        provider   = 1,
        providerId = "pid-$id",
        deletedAt  = deletedAt,
        createdAt  = LocalDateTime.of(2024, 1, 1, 0, 0),
        updatedAt  = LocalDateTime.of(2024, 1, 1, 0, 0),
    )

    private fun mockRepo(staffs: List<Staff> = emptyList()): Repository = object : Repository {
        override suspend fun findByCondition(cond: Condition) = staffs
        override suspend fun findById(id: Long)                                = staffs.firstOrNull { it.id == id }
        override suspend fun findByProvider(provider: Int, providerId: String) = null
        override suspend fun findAllActive()                                    = staffs.filter { it.deletedAt == null }
        override suspend fun save(s: Staff)                                     = s
        override suspend fun updateRole(id: Long, role: Int, updatedBy: Long)   = true
        override suspend fun softDelete(id: Long, deletedBy: Long)              = true
        override suspend fun restore(id: Long)                                  = true
    }

    @Test
    fun `findByCondition maps to ListItem with correct status`() = runBlocking {
        val active  = makeStaff(1L)
        val deleted = makeStaff(2L, deletedAt = LocalDateTime.of(2024, 6, 1, 0, 0))
        val uc      = Interactor(mockRepo(listOf(active, deleted)))
        val result  = uc.findByCondition(Condition())
        assertEquals(2, result.size)
        assertEquals(1, result.first { it.id == 1L }.status)
        assertEquals(0, result.first { it.id == 2L }.status)
    }

    @Test
    fun `updateRole delegates to repository`() = runBlocking {
        var called = false
        val repo = object : Repository by mockRepo() {
            override suspend fun updateRole(id: Long, role: Int, updatedBy: Long): Boolean {
                called = true; return true
            }
        }
        Interactor(repo).updateRole(UpdateRoleDto(id = 1L, role = 1, executorId = 9L))
        assertEquals(true, called)
    }

    @Test
    fun `destroy delegates softDelete to repository`() = runBlocking {
        var deletedId = 0L
        val repo = object : Repository by mockRepo() {
            override suspend fun softDelete(id: Long, deletedBy: Long): Boolean {
                deletedId = id; return true
            }
        }
        Interactor(repo).destroy(DestroyDto(id = 3L, executorId = 9L))
        assertEquals(3L, deletedId)
    }

    @Test
    fun `restore delegates to repository`() = runBlocking {
        var restoredId = 0L
        val repo = object : Repository by mockRepo() {
            override suspend fun restore(id: Long): Boolean { restoredId = id; return true }
        }
        Interactor(repo).restore(5L)
        assertEquals(5L, restoredId)
    }
}
