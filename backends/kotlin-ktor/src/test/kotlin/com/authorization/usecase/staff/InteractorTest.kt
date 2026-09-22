package com.authorization.usecase.staff

import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.Staff
import com.authorization.domain.staff.StaffRole
import com.authorization.support.AppException
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InteractorTest {

    private fun makeStaff(id: Long = 1L, deletedAt: LocalDateTime? = null, role: Int = StaffRole.MEMBER) = Staff(
        id         = id,
        name       = "Staff $id",
        email      = "staff$id@example.com",
        provider   = 1,
        providerId = "pid-$id",
        role       = role,
        deletedAt  = deletedAt,
        createdAt  = LocalDateTime.of(2024, 1, 1, 0, 0),
        updatedAt  = LocalDateTime.of(2024, 1, 1, 0, 0),
    )

    private fun mockRepo(staffs: List<Staff> = emptyList()): Repository = object : Repository {
        override suspend fun countByCondition(cond: Condition)                 = staffs.size
        override suspend fun findByCondition(cond: Condition)                  = staffs
        override suspend fun findById(id: Long)                                = staffs.firstOrNull { it.id == id }
        override suspend fun findByProvider(provider: Int, providerId: String) = null
        override suspend fun findAllActive()                                    = staffs.filter { it.deletedAt == null }
        override suspend fun save(s: Staff)                                     = s
        override suspend fun updateRole(id: Long, role: Int, updatedBy: Long)   = true
        override suspend fun softDelete(id: Long, deletedBy: Long, version: Int) = true
        override suspend fun restore(id: Long)                                  = true
    }

    @Test
    fun `findByCondition maps to ListItem with correct status`() = runBlocking {
        val active  = makeStaff(1L)
        val deleted = makeStaff(2L, deletedAt = LocalDateTime.of(2024, 6, 1, 0, 0))
        val uc      = Interactor(mockRepo(listOf(active, deleted)))
        val (result, _) = uc.findByConditionWithCount(Condition())
        assertEquals(2, result.size)
        assertEquals(1, result.first { it.id == 1L }.status)
        assertEquals(0, result.first { it.id == 2L }.status)
    }

    @Test
    fun `updateRole delegates to repository`() = runBlocking {
        var called = false
        val target   = makeStaff(1L)
        val executor = makeStaff(9L, role = StaffRole.ADMIN)
        val repo = object : Repository by mockRepo(listOf(target, executor)) {
            override suspend fun updateRole(id: Long, role: Int, updatedBy: Long): Boolean {
                called = true; return true
            }
        }
        Interactor(repo).updateRole(UpdateRoleDto(id = 1L, role = 1, executorId = 9L))
        assertEquals(true, called)
    }

    @Test
    fun `updateRole throws unauthorized when executorId is missing`() = runBlocking {
        val target = makeStaff(1L)
        val repo = mockRepo(listOf(target))
        val ex = assertFailsWith<AppException> {
            Interactor(repo).updateRole(UpdateRoleDto(id = 1L, role = StaffRole.ADMIN, executorId = 0L))
        }
        assertEquals(401, ex.statusCode)
    }

    @Test
    fun `updateRole throws forbidden when executor is not admin`() = runBlocking {
        val target   = makeStaff(1L)
        val executor = makeStaff(9L, role = StaffRole.MEMBER)
        val repo = mockRepo(listOf(target, executor))
        val ex = assertFailsWith<AppException> {
            Interactor(repo).updateRole(UpdateRoleDto(id = 1L, role = StaffRole.ADMIN, executorId = 9L))
        }
        assertEquals(403, ex.statusCode)
    }

    @Test
    fun `updateRole throws forbidden when executor is deleted admin`() = runBlocking {
        val target   = makeStaff(1L)
        val executor = makeStaff(9L, role = StaffRole.ADMIN, deletedAt = LocalDateTime.of(2024, 6, 1, 0, 0))
        val repo = mockRepo(listOf(target, executor))
        val ex = assertFailsWith<AppException> {
            Interactor(repo).updateRole(UpdateRoleDto(id = 1L, role = StaffRole.ADMIN, executorId = 9L))
        }
        assertEquals(403, ex.statusCode)
    }

    @Test
    fun `destroy delegates softDelete to repository`() = runBlocking {
        var deletedId = 0L
        val repo = object : Repository by mockRepo(listOf(makeStaff(3L))) {
            override suspend fun softDelete(id: Long, deletedBy: Long, version: Int): Boolean {
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
