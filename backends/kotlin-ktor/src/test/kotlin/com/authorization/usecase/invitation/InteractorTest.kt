package com.authorization.usecase.invitation

import com.authorization.domain.invitation.AuthRepository
import com.authorization.domain.invitation.Repository
import com.authorization.domain.invitation.Vo
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class InteractorTest {

    private fun makeVo(token: String = "tok123", role: Int = 2) = Vo(
        token      = token,
        role       = role,
        url        = "http://localhost:3000/register?token=$token",
        displayUrl = "http://localhost...token=$token",
    )

    private fun mockRepo(
        currentByRole: Vo? = null,
        issued: Vo = makeVo(),
        byToken: Vo? = null,
    ): Repository = object : Repository {
        override suspend fun getCurrentByRole(role: Int)    = currentByRole
        override suspend fun issue(role: Int)               = issued
        override suspend fun findByToken(token: String)     = byToken
    }

    private val stubAuthRepo: AuthRepository = object : AuthRepository {
        override suspend fun store(token: String, role: Int, ttl: Long) {}
        override suspend fun find(token: String): Int? = null
        override suspend fun remove(token: String) {}
    }

    @Test
    fun `current returns vo when found`() = runBlocking {
        val uc = Interactor(mockRepo(currentByRole = makeVo("abc")), stubAuthRepo)
        assertEquals("abc", uc.current(2).token)
    }

    @Test
    fun `current throws when not found`() = runBlocking {
        val uc = Interactor(mockRepo(currentByRole = null), stubAuthRepo)
        var threw = false
        try { uc.current(2) } catch (_: Exception) { threw = true }
        assertEquals(true, threw)
    }

    @Test
    fun `issue returns new vo`() = runBlocking {
        val uc = Interactor(mockRepo(issued = makeVo("newtoken")), stubAuthRepo)
        val vo = uc.issue(2)
        assertEquals("newtoken", vo.token)
    }

    @Test
    fun `findByToken returns vo when found`() = runBlocking {
        val uc = Interactor(mockRepo(byToken = makeVo("xyz")), stubAuthRepo)
        val vo = uc.findByToken(FindByTokenDto("xyz"))
        assertEquals("xyz", vo.token)
    }

    @Test
    fun `findByToken throws when not found`() = runBlocking {
        val uc = Interactor(mockRepo(byToken = null), stubAuthRepo)
        var threw = false
        try { uc.findByToken(FindByTokenDto("bad")) } catch (_: Exception) { threw = true }
        assertEquals(true, threw)
    }
}
