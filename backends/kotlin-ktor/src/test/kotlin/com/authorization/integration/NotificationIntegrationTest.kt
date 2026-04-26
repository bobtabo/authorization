package com.authorization.integration

import com.authorization.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.*

class NotificationIntegrationTest {

    @BeforeTest
    fun setUp() {
        TestHelper.db
        TestHelper.truncateTables()
    }

    @Test
    fun `GET api notifications counts returns unread and total when authenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff = TestHelper.createStaff()
        TestHelper.createNotification(staff.id, "通知1")
        TestHelper.createNotification(staff.id, "通知2", read = true)
        val response = client.get("/api/notifications/counts") {
            header(HttpHeaders.Cookie, "staff_id=${staff.id}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(2L, body["total"]!!.jsonPrimitive.long)
        assertEquals(1L, body["unread"]!!.jsonPrimitive.long)
    }

    @Test
    fun `GET api notifications counts returns 401 when unauthenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/notifications/counts")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET api notifications returns notification list when authenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff = TestHelper.createStaff()
        TestHelper.createNotification(staff.id, "通知1")
        TestHelper.createNotification(staff.id, "通知2")
        val response = client.get("/api/notifications") {
            header(HttpHeaders.Cookie, "staff_id=${staff.id}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(2, body["items"]!!.jsonArray.size)
    }

    @Test
    fun `GET api notifications returns 401 when unauthenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/notifications")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PATCH api notifications bulk marks read when authenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff = TestHelper.createStaff()
        TestHelper.createNotification(staff.id, "通知1")
        TestHelper.createNotification(staff.id, "通知2")
        val response = client.patch("/api/notifications") {
            header(HttpHeaders.Cookie, "staff_id=${staff.id}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PATCH api notifications returns 401 when unauthenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.patch("/api/notifications")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PATCH api notifications id marks single notification as read`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff = TestHelper.createStaff()
        val notif = TestHelper.createNotification(staff.id, "通知1")
        val response = client.patch("/api/notifications/${notif.id}")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(notif.id, body["id"]!!.jsonPrimitive.long)
    }
}
