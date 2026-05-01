package com.authorization.usecase.client

import com.authorization.domain.client.Client
import com.authorization.domain.client.ClientStatus
import com.authorization.domain.client.Condition
import com.authorization.domain.client.Repository
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InteractorTest {

    private fun makeClient(id: Long = 1L) = Client(
        id          = id,
        name        = "Test Client",
        identifier  = "abc123",
        email       = "client@example.com",
        accessToken = "token123",
        privateKey  = "priv",
        publicKey   = "pub",
        fingerprint = "SHA256:xxx",
        status      = ClientStatus.ACTIVE,
        createdAt   = LocalDateTime.of(2024, 1, 1, 0, 0),
        updatedAt   = LocalDateTime.of(2024, 1, 1, 0, 0),
    )

    private fun mockRepo(
        byCondition: List<Client>  = emptyList(),
        byId: Client?              = null,
        byToken: Client?           = null,
        byIdentifier: Client?      = null,
        saveResult: Client?        = null,
    ): Repository = object : Repository {
        override suspend fun findByCondition(cond: Condition) = byCondition
        override suspend fun findById(id: Long)               = byId
        override suspend fun findByAccessToken(token: String) = byToken
        override suspend fun findByIdentifier(id: String)     = byIdentifier
        override suspend fun save(c: Client)                  = saveResult ?: c
        override suspend fun softDelete(id: Long, deletedBy: Long) = Unit
    }

    @Test
    fun `findByCondition returns clients from repo`() = runBlocking {
        val clients = listOf(makeClient(1L), makeClient(2L))
        val uc      = Interactor(mockRepo(byCondition = clients))
        val result  = uc.findByCondition(ListConditionDto())
        assertEquals(2, result.size)
    }

    @Test
    fun `findById returns client when found`() = runBlocking {
        val uc     = Interactor(mockRepo(byId = makeClient(5L)))
        val result = uc.findById(5L)
        assertEquals(5L, result.id)
    }

    @Test
    fun `findById throws when not found`() = runBlocking {
        val uc = Interactor(mockRepo(byId = null))
        var threw = false
        try { uc.findById(99L) } catch (_: Exception) { threw = true }
        assertEquals(true, threw)
    }

    @Test
    fun `store returns StoreResultVo with generated token and id`() = runBlocking {
        val saved  = makeClient(10L)
        val uc     = Interactor(mockRepo(saveResult = saved))
        val dto    = StoreDto(name = "New Client", email = "n@example.com", executorId = 1L)
        val result = uc.store(dto)
        assertEquals(10L, result.id)
        assertEquals("Test Client", result.name)
    }

    @Test
    fun `update applies name change and returns DetailVo`() = runBlocking {
        val original = makeClient(3L)
        val uc       = Interactor(mockRepo(byId = original, saveResult = original.copy(name = "Updated")))
        val vo       = uc.update(UpdateDto(id = 3L, name = "Updated", executorId = 1L))
        assertEquals("Updated", vo.name)
        assertEquals(3L, vo.id)
    }

    @Test
    fun `update sets startAt when status changes to ACTIVE`() = runBlocking {
        val original = makeClient(3L).copy(status = ClientStatus.INACTIVE, startAt = null)
        var saved: Client? = null
        val repo = object : Repository by mockRepo(byId = original) {
            override suspend fun save(c: Client): Client { saved = c; return c }
        }
        Interactor(repo).update(UpdateDto(id = 3L, status = ClientStatus.ACTIVE, executorId = 1L))
        assertTrue(saved?.startAt != null)
    }

    @Test
    fun `destroy throws when client not found`() = runBlocking {
        val uc = Interactor(mockRepo(byId = null))
        var threw = false
        try { uc.destroy(99L, 1L) } catch (_: Exception) { threw = true }
        assertEquals(true, threw)
    }

    @Test
    fun `findByAccessToken returns null when not found`() = runBlocking {
        val uc = Interactor(mockRepo(byToken = null))
        assertNull(uc.findByAccessToken("bad_token"))
    }

    @Test
    fun `findByIdentifier returns client when found`() = runBlocking {
        val uc = Interactor(mockRepo(byIdentifier = makeClient(7L)))
        assertTrue(uc.findByIdentifier("abc123") != null)
    }
}
