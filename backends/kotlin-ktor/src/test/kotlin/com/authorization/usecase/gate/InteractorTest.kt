package com.authorization.usecase.gate

import com.authorization.config.AppConfig
import com.authorization.config.Config
import com.authorization.config.DbConfig
import com.authorization.config.JwtConfig
import com.authorization.config.MailConfig
import com.authorization.config.OAuthConfig
import com.authorization.config.RedisConfig
import com.authorization.domain.client.Client
import com.authorization.domain.client.ClientStatus
import com.authorization.domain.client.Condition
import com.authorization.domain.client.Repository as ClientRepository
import com.authorization.domain.gate.CacheRepository
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InteractorTest {

    private fun makeClient() = Client(
        id          = 1L,
        name        = "Test",
        identifier  = "test-id",
        accessToken = "access-tok",
        privateKey  = "priv",
        publicKey   = "pub",
        fingerprint = "SHA256:fp",
        status      = ClientStatus.ACTIVE,
        createdAt   = LocalDateTime.of(2024, 1, 1, 0, 0),
        updatedAt   = LocalDateTime.of(2024, 1, 1, 0, 0),
    )

    private fun makeConfig() = Config(
        app = AppConfig(
            env = "test", port = 8080,
            runtime = "kotlin",
            frontendUrl = "http://localhost:3000",
            staffCookieLifetime = 60L,
            notificationDefaultLimit = 10L,
            cachePrefix = "test",
        ),
        db    = DbConfig(dsn = "", username = "", password = ""),
        redis = RedisConfig(host = "localhost", port = 6379, password = "", db = 0),
        oauth = OAuthConfig(
            googleClientId = "", googleClientSecret = "", googleRedirectUrl = "",
            githubClientId = "", githubClientSecret = "", githubRedirectUrl = "",
        ),
        jwt   = JwtConfig(issuer = "authorization", algorithm = "RS256", ttl = 1800L, cacheTtl = 1800L),
        mail  = MailConfig(
            host = "localhost", port = "1025", username = "", password = "",
            fromAddress = "no-reply@example.com", appName = "Test", appEnv = "test",
        ),
    )

    private fun mockClientRepo(
        byToken: Client? = null,
        byIdentifier: Client? = null,
    ): ClientRepository = object : ClientRepository {
        override suspend fun countByCondition(cond: Condition) = 0
        override suspend fun findByCondition(cond: Condition) = emptyList<Client>()
        override suspend fun findById(id: Long)               = null
        override suspend fun findByAccessToken(token: String) = byToken
        override suspend fun findByIdentifier(id: String)     = byIdentifier
        override suspend fun save(c: Client)                  = c
        override suspend fun softDelete(id: Long, deletedBy: Long, version: Int) = Unit
    }

    private fun mockCache(
        getCached: String? = null,
    ): CacheRepository = object : CacheRepository {
        override suspend fun getJwt(identifier: String, memberId: String) = getCached
        override suspend fun putJwt(identifier: String, memberId: String, token: String, ttl: Long) = Unit
    }

    @Test
    fun `issueToken returns error when client not found`() = runBlocking {
        val uc  = Interactor(mockClientRepo(byToken = null), mockCache(), makeConfig())
        val dto = IssueDto(accessToken = "bad", memberId = "m1")
        var threw = false
        try { uc.issueToken(dto) } catch (_: Exception) { threw = true }
        assertTrue(threw)
    }

    @Test
    fun `issueToken returns cached token when cache hit`() = runBlocking {
        val uc = Interactor(
            mockClientRepo(byToken = makeClient()),
            mockCache(getCached = "cached.jwt.token"),
            makeConfig(),
        )
        val dto    = IssueDto(accessToken = "access-tok", memberId = "m1")
        val result = uc.issueToken(dto)
        assertEquals("cached.jwt.token", result.token)
    }

    @Test
    fun `verify returns error when client not found`() = runBlocking {
        val uc  = Interactor(mockClientRepo(byIdentifier = null), mockCache(), makeConfig())
        val dto = VerifyDto(identifier = "unknown", token = "bad.jwt.token")
        var threw = false
        try { uc.verify(dto) } catch (_: Exception) { threw = true }
        assertTrue(threw)
    }
}
