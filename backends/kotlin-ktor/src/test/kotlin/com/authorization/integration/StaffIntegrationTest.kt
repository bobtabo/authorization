package com.authorization.integration

import com.authorization.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import com.authorization.infrastructure.model.Staffs
import java.time.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.*

class StaffIntegrationTest {

    @BeforeTest
    fun setUp() {
        TestHelper.db
        TestHelper.truncateTables()
    }

    @Test
    fun `GET api staffs returns list`() = testApplication {
        application { module(TestHelper.cfg) }
        TestHelper.createStaff(email = "s1@example.com")
        TestHelper.createStaff(email = "s2@example.com")
        val response = client.get("/api/staffs")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(2, body["items"]!!.jsonArray.size)
    }

    @Test
    fun `GET api staffs returns empty list when none exist`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/staffs")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(0, body["items"]!!.jsonArray.size)
    }

    @Test
    fun `PATCH api staffs id updateRole updates role and returns id`() = testApplication {
        application { module(TestHelper.cfg) }
        val target   = TestHelper.createStaff(email = "target@example.com", role = 2)
        val executor = TestHelper.createStaff(email = "exec@example.com", role = 1)
        val response = client.patch("/api/staffs/${target.id}/updateRole") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Cookie, "staff_id=${executor.id}")
            setBody(buildJsonObject { put("role", 1) }.toString())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(target.id, body["id"]!!.jsonPrimitive.long)
    }

    @Test
    fun `PATCH api staffs id restore restores deleted staff and returns id`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff = TestHelper.createStaff()
        transaction(TestHelper.db) {
            Staffs.update({ Staffs.id eq staff.id }) {
                it[Staffs.deletedAt] = LocalDateTime.now()
                it[Staffs.updatedAt] = LocalDateTime.now()
            }
        }
        val response = client.patch("/api/staffs/${staff.id}/restore")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(staff.id, body["id"]!!.jsonPrimitive.long)
    }

    @Test
    fun `DELETE api staffs id delete soft-deletes staff and returns id`() = testApplication {
        application { module(TestHelper.cfg) }
        val executor = TestHelper.createStaff(email = "exec@example.com")
        val target   = TestHelper.createStaff(email = "target@example.com")
        val response = client.delete("/api/staffs/${target.id}/delete") {
            header(HttpHeaders.Cookie, "staff_id=${executor.id}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(target.id, body["id"]!!.jsonPrimitive.long)
    }
}
