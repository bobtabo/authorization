package com.authorization.integration

import com.authorization.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.*

class AuthIntegrationTest {

    @BeforeTest
    fun setUp() {
        TestHelper.db
        TestHelper.truncateTables()
    }

    @Test
    fun `GET api auth me returns profile when authenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff = TestHelper.createStaff()
        val response = client.get("/api/auth/me") {
            header(HttpHeaders.Cookie, "staff_id=${staff.id}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(staff.id, body["staff_id"]!!.jsonPrimitive.long)
    }

    @Test
    fun `GET api auth me returns 401 when unauthenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET api auth login returns info when authenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff = TestHelper.createStaff()
        val response = client.get("/api/auth/login") {
            header(HttpHeaders.Cookie, "staff_id=${staff.id}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(staff.id, body["staff_id"]!!.jsonPrimitive.long)
    }

    @Test
    fun `GET api auth login returns 401 when unauthenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/auth/login")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET api auth logout returns 200`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/auth/logout")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET api auth github redirect issues nonce cookie and embeds it in state`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = createClient { followRedirects = false }
        val response = c.get("/api/auth/github/redirect?token=inv-token")
        assertEquals(HttpStatusCode.Found, response.status)
        val setCookie = response.headers.getAll(HttpHeaders.SetCookie)!!.first { it.startsWith("oauth_state=") }
        val nonce = Regex("oauth_state=([0-9a-f]+)").find(setCookie)?.groupValues?.get(1)
        assertNotNull(nonce)
        assertTrue(setCookie.contains("HttpOnly"))
        val location = response.headers[HttpHeaders.Location]!!
        assertTrue(location.contains("state=" + java.net.URLEncoder.encode("${TestHelper.cfg.app.runtime}|$nonce|inv-token", "UTF-8")))
    }

    @Test
    fun `GET api auth github callback without nonce cookie redirects to 400`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = createClient { followRedirects = false }
        val response = c.get("/api/auth/github/callback?code=abc&state=kotlin%7Cnonce123")
        assertEquals(HttpStatusCode.Found, response.status)
        assertTrue(response.headers[HttpHeaders.Location]!!.endsWith("/error?code=400"))
    }

    @Test
    fun `GET api auth google callback with mismatched nonce redirects to 400 and clears cookie`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = createClient { followRedirects = false }
        val response = c.get("/api/auth/google/callback?code=abc&state=kotlin%7Cwrong") {
            header(HttpHeaders.Cookie, "oauth_state=right")
        }
        assertEquals(HttpStatusCode.Found, response.status)
        assertTrue(response.headers[HttpHeaders.Location]!!.endsWith("/error?code=400"))
        val cleared = response.headers.getAll(HttpHeaders.SetCookie)!!.first { it.startsWith("oauth_state=") }
        assertTrue(cleared.contains("Max-Age=0"))
    }

    @Test
    fun `GET api auth invitation returns invitation when valid token`() = testApplication {
        application { module(TestHelper.cfg) }
        val inv = TestHelper.createInvitation("test-token-xyz")
        val response = client.get("/api/auth/invitation/${inv.token}")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(inv.token, body["token"]!!.jsonPrimitive.content)
        assertTrue(body["found"]!!.jsonPrimitive.boolean)
    }
}
