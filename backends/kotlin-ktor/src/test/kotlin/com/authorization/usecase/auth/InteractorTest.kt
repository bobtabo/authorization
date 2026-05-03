package com.authorization.usecase.auth

import com.authorization.domain.invitation.AuthRepository as InvAuthRepository
import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.Staff
import com.authorization.domain.staff.StaffRole
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class InteractorTest {

    private fun makeStaff(id: Long = 1L) = Staff(
        id         = id,
        name       = "Test Staff",
        email      = "staff@example.com",
        provider   = 1,
        providerId = "google-123",
        createdAt  = LocalDateTime.of(2024, 1, 1, 0, 0),
        updatedAt  = LocalDateTime.of(2024, 1, 1, 0, 0),
    )

    private fun mockRepo(
        findById: Staff? = null,
        findByProvider: Staff? = null,
        saveResult: Staff? = null,
    ): Repository = object : Repository {
        override suspend fun findByCondition(cond: Condition) = emptyList<Staff>()
        override suspend fun findById(id: Long)                                     = findById
        override suspend fun findByProvider(provider: Int, providerId: String)      = findByProvider
        override suspend fun findAllActive()                                         = emptyList<Staff>()
        override suspend fun save(s: Staff)                                          = saveResult ?: s
        override suspend fun updateRole(id: Long, role: Int, updatedBy: Long)        = true
        override suspend fun softDelete(id: Long, deletedBy: Long, version: Int)      = true
        override suspend fun restore(id: Long)                                       = true
    }

    private fun mockInvAuthRepo(storedToken: String? = null): InvAuthRepository = object : InvAuthRepository {
        override suspend fun store(token: String, ttl: Long) {}
        override suspend fun find(token: String) = storedToken
        override suspend fun remove(token: String) {}
    }

    private fun makeUc(staffRepo: Repository, storedToken: String? = null) =
        Interactor(staffRepo, mockInvAuthRepo(storedToken))

    @Test
    fun `findUser returns staff when found`() = runBlocking {
        val uc = makeUc(mockRepo(findById = makeStaff(1L)))
        val result = uc.findUser(1L)
        assertEquals(1L, result.id)
        assertEquals("Test Staff", result.name)
    }

    @Test
    fun `findUser throws when not found`() = runBlocking {
        val uc = makeUc(mockRepo(findById = null))
        var threw = false
        try { uc.findUser(99L) } catch (_: Exception) { threw = true }
        assertEquals(true, threw)
    }

    @Test
    fun `login creates new staff with valid invitation token`() = runBlocking {
        val uc     = makeUc(mockRepo(findByProvider = null), storedToken = "tok")
        val dto    = LoginDto(provider = 1, providerId = "new-id", name = "New User", email = "new@example.com", avatar = null, invitationToken = "tok")
        val result = uc.login(dto)
        assertEquals("New User", result.name)
        assertEquals(StaffRole.MEMBER, result.role)
    }

    @Test
    fun `login throws when new staff has no invitation token`() = runBlocking {
        val uc  = makeUc(mockRepo(findByProvider = null))
        val dto = LoginDto(provider = 1, providerId = "new-id", name = "New User", email = "new@example.com", avatar = null)
        var threw = false
        try { uc.login(dto) } catch (_: Exception) { threw = true }
        assertEquals(true, threw)
    }

    @Test
    fun `login updates existing staff avatar and lastLoginAt`() = runBlocking {
        val existing = makeStaff(5L)
        val uc       = makeUc(mockRepo(findByProvider = existing, saveResult = existing.copy(avatar = "new-avatar")))
        val dto      = LoginDto(provider = 1, providerId = "google-123", name = "Test Staff", email = "staff@example.com", avatar = "new-avatar")
        val result   = uc.login(dto)
        assertEquals(5L, result.id)
        assertTrue(result.avatar != null)
    }
}
