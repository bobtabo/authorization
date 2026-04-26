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
import kotlinx.serialization.json.*

class GateIntegrationTest {

    @BeforeTest
    fun setUp() {
        TestHelper.db
        TestHelper.truncateTables()
    }

    @Test
    fun `GET api gate issue returns JWT token`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = TestHelper.createClient()
        val response = client.get("/api/gate/issue?member=user-001") {
            header(HttpHeaders.Authorization, "Bearer ${c.accessToken}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertNotNull(body["token"]!!.jsonPrimitive.content)
    }

    @Test
    fun `GET api gate issue returns 400 when member param missing`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/gate/issue")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("member_required", body["error"]!!.jsonPrimitive.content)
    }

    @Test
    fun `GET api gate verify returns claims for valid token`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = TestHelper.createClient()
        val issueResponse = client.get("/api/gate/issue?member=user-001") {
            header(HttpHeaders.Authorization, "Bearer ${c.accessToken}")
        }
        val token = Json.parseToJsonElement(issueResponse.bodyAsText()).jsonObject["token"]!!.jsonPrimitive.content

        val verifyResponse = client.get("/api/gate/client/${c.identifier}/verify?token=$token")
        assertEquals(HttpStatusCode.OK, verifyResponse.status)
        val body = Json.parseToJsonElement(verifyResponse.bodyAsText()).jsonObject
        assertEquals("user-001", body["sub"]!!.jsonPrimitive.content)
    }

    @Test
    fun `GET api gate verify returns 400 when token missing`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = TestHelper.createClient()
        val response = client.get("/api/gate/client/${c.identifier}/verify")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("token_required", body["error"]!!.jsonPrimitive.content)
    }
}
