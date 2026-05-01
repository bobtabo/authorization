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
        val inv = TestHelper.createInvitation()
        val response = client.get("/api/admin/invitation")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body["found"]!!.jsonPrimitive.boolean)
        assertEquals(inv.token, body["token"]!!.jsonPrimitive.content)
    }

    @Test
    fun `GET api admin invitation issue issues new invitation`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/admin/invitation/issue")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body["found"]!!.jsonPrimitive.boolean)
        assertNotNull(body["token"]!!.jsonPrimitive.content)
    }
}
