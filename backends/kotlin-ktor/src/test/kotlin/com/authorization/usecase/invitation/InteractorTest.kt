package com.authorization.usecase.invitation

import com.authorization.domain.invitation.Repository
import com.authorization.domain.invitation.Vo
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class InteractorTest {

    private fun makeVo(token: String = "tok123") = Vo(
        token      = token,
        url        = "http://localhost:3000/register?token=$token",
        displayUrl = "http://localhost...token=$token",
    )

    private fun mockRepo(
        current: Vo? = null,
        issued: Vo = makeVo(),
        byToken: Vo? = null,
    ): Repository = object : Repository {
        override suspend fun getCurrent()               = current
        override suspend fun issue()                    = issued
        override suspend fun findByToken(token: String) = byToken
    }

    @Test
    fun `current returns vo when found`() = runBlocking {
        val uc = Interactor(mockRepo(current = makeVo("abc")))
        assertEquals("abc", uc.current().token)
    }

    @Test
    fun `current throws when not found`() = runBlocking {
        val uc = Interactor(mockRepo(current = null))
        var threw = false
        try { uc.current() } catch (_: Exception) { threw = true }
        assertEquals(true, threw)
    }

    @Test
    fun `issue returns new vo`() = runBlocking {
        val uc = Interactor(mockRepo(issued = makeVo("newtoken")))
        val vo = uc.issue()
        assertEquals("newtoken", vo.token)
    }

    @Test
    fun `findByToken returns vo when found`() = runBlocking {
        val uc = Interactor(mockRepo(byToken = makeVo("xyz")))
        val vo = uc.findByToken(FindByTokenDto("xyz"))
        assertEquals("xyz", vo.token)
    }

    @Test
    fun `findByToken throws when not found`() = runBlocking {
        val uc = Interactor(mockRepo(byToken = null))
        var threw = false
        try { uc.findByToken(FindByTokenDto("bad")) } catch (_: Exception) { threw = true }
        assertEquals(true, threw)
    }
}
