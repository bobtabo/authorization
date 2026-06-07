package com.authorization.usecase.client

import com.authorization.domain.client.Client
import com.authorization.domain.client.ClientStatus
import com.authorization.domain.client.Condition
import com.authorization.domain.client.Repository
import com.authorization.support.AppException
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
        override suspend fun countByCondition(cond: Condition) = 0
        override suspend fun findByCondition(cond: Condition) = byCondition
        override suspend fun findById(id: Long)               = byId
        override suspend fun findByAccessToken(token: String) = byToken
        override suspend fun findByIdentifier(id: String)     = byIdentifier
        override suspend fun save(c: Client)                  = saveResult ?: c
        override suspend fun softDelete(id: Long, deletedBy: Long, version: Int) = Unit
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

    @Test
    fun `getQr returns QrVo with correct deeplink_url`() = runBlocking {
        val uc = Interactor(mockRepo(byIdentifier = makeClient(1L)))
        val vo = uc.getQr(QrDto("abc123"))
        assertEquals("abc123", vo.identifier)
        assertEquals("authgateway://clients/abc123/info", vo.deeplinkUrl)
    }

    @Test
    fun `getQr throws AppException when client not found`() = runBlocking {
        val uc = Interactor(mockRepo(byIdentifier = null))
        assertFailsWith<AppException> { uc.getQr(QrDto("notexist")) }
        Unit
    }

    @Test
    fun `getInfo returns InfoVo with identifier name and status`() = runBlocking {
        val uc = Interactor(mockRepo(byIdentifier = makeClient(1L)))
        val vo = uc.getInfo(InfoDto("abc123"))
        assertEquals("abc123", vo.identifier)
        assertEquals("Test Client", vo.name)
        assertEquals(ClientStatus.ACTIVE, vo.status)
    }

    @Test
    fun `getInfo throws AppException when client not found`() = runBlocking {
        val uc = Interactor(mockRepo(byIdentifier = null))
        assertFailsWith<AppException> { uc.getInfo(InfoDto("notexist")) }
        Unit
    }

    @Test
    fun `start returns access token and activates inactive client`() = runBlocking {
        val inactive = makeClient(1L).copy(status = ClientStatus.INACTIVE, startAt = null)
        var saved: Client? = null
        val repo = object : Repository by mockRepo(byIdentifier = inactive) {
            override suspend fun save(c: Client): Client { saved = c; return c }
        }
        val vo = Interactor(repo).start(StartDto("abc123"))
        assertEquals("token123", vo.accessToken)
        assertEquals(ClientStatus.ACTIVE, saved?.status)
        assertTrue(saved?.startAt != null)
        assertNull(saved?.stopAt)
    }

    @Test
    fun `start returns access token without update when already active`() = runBlocking {
        val active = makeClient(1L).copy(status = ClientStatus.ACTIVE)
        var saveCalled = false
        val repo = object : Repository by mockRepo(byIdentifier = active) {
            override suspend fun save(c: Client): Client { saveCalled = true; return c }
        }
        val vo = Interactor(repo).start(StartDto("abc123"))
        assertEquals("token123", vo.accessToken)
        assertEquals(false, saveCalled)
    }

    @Test
    fun `start throws AppException when client not found`() = runBlocking {
        val uc = Interactor(mockRepo(byIdentifier = null))
        assertFailsWith<AppException> { uc.start(StartDto("notexist")) }
        Unit
    }

    @Test
    fun `stop suspends active client`() = runBlocking {
        val active = makeClient(1L).copy(status = ClientStatus.ACTIVE)
        var saved: Client? = null
        val repo = object : Repository by mockRepo(byIdentifier = active) {
            override suspend fun save(c: Client): Client { saved = c; return c }
        }
        Interactor(repo).stop(StopDto("abc123"))
        assertEquals(ClientStatus.SUSPENDED, saved?.status)
        assertTrue(saved?.stopAt != null)
    }

    @Test
    fun `stop does nothing when client is not active`() = runBlocking {
        val suspended = makeClient(1L).copy(status = ClientStatus.SUSPENDED)
        var saveCalled = false
        val repo = object : Repository by mockRepo(byIdentifier = suspended) {
            override suspend fun save(c: Client): Client { saveCalled = true; return c }
        }
        Interactor(repo).stop(StopDto("abc123"))
        assertEquals(false, saveCalled)
    }

    @Test
    fun `stop throws AppException when client not found`() = runBlocking {
        val uc = Interactor(mockRepo(byIdentifier = null))
        assertFailsWith<AppException> { uc.stop(StopDto("notexist")) }
        Unit
    }
}
