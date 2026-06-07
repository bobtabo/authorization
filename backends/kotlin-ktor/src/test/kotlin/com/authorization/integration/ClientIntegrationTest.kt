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

class ClientIntegrationTest {

    @BeforeTest
    fun setUp() {
        TestHelper.db
        TestHelper.truncateTables()
    }

    @Test
    fun `GET api clients returns list`() = testApplication {
        application { module(TestHelper.cfg) }
        TestHelper.createClient()
        TestHelper.createClient()
        val response = client.get("/api/clients")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(2, body["data"]!!.jsonArray.size)
    }

    @Test
    fun `GET api clients returns empty list when none exist`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/clients")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(0, Json.parseToJsonElement(response.bodyAsText()).jsonObject["data"]!!.jsonArray.size)
    }

    @Test
    fun `GET api clients id returns client detail`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = TestHelper.createClient()
        val response = client.get("/api/clients/${c.id}")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(c.identifier, body["identifier"]!!.jsonPrimitive.content)
    }

    @Test
    fun `GET api clients nonexistent id returns error`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/clients/99999")
        assertTrue(response.status != HttpStatusCode.OK)
    }

    @Test
    fun `POST api clients store registers client and returns 201`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff = TestHelper.createStaff()
        val response = client.post("/api/clients/store") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Cookie, "staff_id=${staff.id}")
            setBody(buildJsonObject {
                put("name",      "新規クライアント株式会社")
                put("post_code", "100-0001")
                put("pref",      "東京都")
                put("city",      "千代田区")
                put("address",   "千代田1-1")
                put("tel",       "0312345678")
                put("email",     "new-client@example.com")
            }.toString())
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertNotNull(body["id"])
    }

    @Test
    fun `PUT api clients id update updates client`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff  = TestHelper.createStaff()
        val c      = TestHelper.createClient()
        val response = client.put("/api/clients/${c.id}/update") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Cookie, "staff_id=${staff.id}")
            setBody(buildJsonObject { put("name", "更新後クライアント名") }.toString())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("更新後クライアント名", body["name"]!!.jsonPrimitive.content)
    }

    @Test
    fun `DELETE api clients id delete removes client`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff  = TestHelper.createStaff()
        val c      = TestHelper.createClient()
        val response = client.delete("/api/clients/${c.id}/delete") {
            header(HttpHeaders.Cookie, "staff_id=${staff.id}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // --- スマホ連携 API ---

    @Test
    fun `GET api clients identifier qr returns qr data`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = TestHelper.createClient()
        val response = client.get("/api/clients/${c.identifier}/qr")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(c.identifier, body["identifier"]!!.jsonPrimitive.content)
        assertEquals("authgateway://clients/${c.identifier}/info", body["deeplink_url"]!!.jsonPrimitive.content)
    }

    @Test
    fun `GET api clients nonexistent identifier qr returns error`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/clients/nonexistent-identifier/qr")
        assertTrue(response.status != HttpStatusCode.OK)
    }

    @Test
    fun `GET api clients identifier info returns client info`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = TestHelper.createClient()
        val response = client.get("/api/clients/${c.identifier}/info")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(c.identifier, body["identifier"]!!.jsonPrimitive.content)
        assertNotNull(body["name"])
        assertNotNull(body["status"])
    }

    @Test
    fun `GET api clients nonexistent identifier info returns error`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/clients/nonexistent-identifier/info")
        assertTrue(response.status != HttpStatusCode.OK)
    }

    @Test
    fun `PATCH api clients identifier start returns access token`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = TestHelper.createClient()
        val response = client.patch("/api/clients/${c.identifier}/start")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertNotNull(body["access_token"])
    }

    @Test
    fun `PATCH api clients nonexistent identifier start returns error`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.patch("/api/clients/nonexistent-identifier/start")
        assertTrue(response.status != HttpStatusCode.OK)
    }

    @Test
    fun `PATCH api clients identifier stop returns empty body`() = testApplication {
        application { module(TestHelper.cfg) }
        val c = TestHelper.createClient()
        val response = client.patch("/api/clients/${c.identifier}/stop")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(0, body.size)
    }

    @Test
    fun `PATCH api clients nonexistent identifier stop returns error`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.patch("/api/clients/nonexistent-identifier/stop")
        assertTrue(response.status != HttpStatusCode.OK)
    }
}
