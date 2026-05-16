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

class AdminInvitationIntegrationTest {

    @BeforeTest
    fun setUp() {
        TestHelper.db
        TestHelper.truncateTables()
    }

    @Test
    fun `GET api admin invitation returns current invitation`() = testApplication {
        application { module(TestHelper.cfg) }
        val inv = TestHelper.createInvitation(role = 2)
        val response = client.get("/api/admin/invitation?role=2")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body["found"]!!.jsonPrimitive.boolean)
        assertEquals(inv.token, body["token"]!!.jsonPrimitive.content)
    }

    @Test
    fun `GET api admin invitation issue issues new invitation`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff = TestHelper.createStaff()
        val response = client.get("/api/admin/invitation/issue?role=2") {
            header(HttpHeaders.Cookie, "staff_id=${staff.id}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body["found"]!!.jsonPrimitive.boolean)
        assertNotNull(body["token"]!!.jsonPrimitive.content)
    }

    @Test
    fun `GET api admin invitation issue returns 401 when unauthenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/admin/invitation/issue?role=2")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET api admin invitation returns 400 for invalid role`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/admin/invitation?role=3")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
