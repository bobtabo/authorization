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
        assertEquals(2, body["data"]!!.jsonArray.size)
        assert(body["pager"] != null)
    }

    @Test
    fun `GET api staffs returns empty list when none exist`() = testApplication {
        application { module(TestHelper.cfg) }
        val response = client.get("/api/staffs")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(0, body["data"]!!.jsonArray.size)
    }

    @Test
    fun `GET api staffs keyword underscore is not treated as wildcard`() = testApplication {
        application { module(TestHelper.cfg) }
        TestHelper.createStaff(name = "アンダースコア", email = "a_b@example.com")
        TestHelper.createStaff(name = "エックス", email = "axb@example.com")
        val response = client.get("/api/staffs?keyword=a_b")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(1, body["data"]!!.jsonArray.size)
    }

    @Test
    fun `PATCH api staffs id updateRole updates role and returns id`() = testApplication {
        application { module(TestHelper.cfg) }
        val target   = TestHelper.createStaff(email = "target@example.com", role = 2)
        val executor = TestHelper.createStaff(email = "exec@example.com", role = 1)
        val response = client.patch("/api/staffs/${target.id}/updateRole") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Cookie, "staff_id=${TestHelper.signStaffCookie(executor.id)}")
            setBody(buildJsonObject { put("role", 1) }.toString())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(target.id, body["id"]!!.jsonPrimitive.long)
    }

    @Test
    fun `PATCH api staffs id updateRole unauthenticated returns 401`() = testApplication {
        application { module(TestHelper.cfg) }
        val target = TestHelper.createStaff(email = "target-unauth@example.com", role = 2)
        val response = client.patch("/api/staffs/${target.id}/updateRole") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("role", 1) }.toString())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PATCH api staffs id updateRole non-admin executor returns 403`() = testApplication {
        application { module(TestHelper.cfg) }
        val target   = TestHelper.createStaff(email = "target-member@example.com", role = 2)
        val executor = TestHelper.createStaff(email = "member-executor@example.com", role = 2)
        val response = client.patch("/api/staffs/${target.id}/updateRole") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Cookie, "staff_id=${TestHelper.signStaffCookie(executor.id)}")
            setBody(buildJsonObject { put("role", 1) }.toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PATCH api staffs id updateRole deleted admin executor returns 403`() = testApplication {
        application { module(TestHelper.cfg) }
        val target   = TestHelper.createStaff(email = "target-deleted-admin@example.com", role = 2)
        val executor = TestHelper.createStaff(email = "deleted-admin-executor@example.com", role = 1)
        // 署名済みクッキーは有効だが、実行者は既に無効化（論理削除）されている状態を再現する。
        transaction(TestHelper.db) {
            Staffs.update({ Staffs.id eq executor.id }) {
                it[Staffs.deletedAt] = LocalDateTime.now()
                it[Staffs.updatedAt] = LocalDateTime.now()
            }
        }
        val response = client.patch("/api/staffs/${target.id}/updateRole") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Cookie, "staff_id=${TestHelper.signStaffCookie(executor.id)}")
            setBody(buildJsonObject { put("role", 1) }.toString())
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PATCH api staffs id restore restores deleted staff and returns id`() = testApplication {
        application { module(TestHelper.cfg) }
        val executor = TestHelper.createStaff(email = "restore-exec@example.com", role = 1)
        val staff = TestHelper.createStaff()
        transaction(TestHelper.db) {
            Staffs.update({ Staffs.id eq staff.id }) {
                it[Staffs.deletedAt] = LocalDateTime.now()
                it[Staffs.updatedAt] = LocalDateTime.now()
            }
        }
        val response = client.patch("/api/staffs/${staff.id}/restore") {
            header(HttpHeaders.Cookie, "staff_id=${TestHelper.signStaffCookie(executor.id)}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(staff.id, body["id"]!!.jsonPrimitive.long)
    }

    @Test
    fun `PATCH api staffs id restore returns 401 when unauthenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff = TestHelper.createStaff()
        val response = client.patch("/api/staffs/${staff.id}/restore")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PATCH api staffs id restore non-admin executor returns 403`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff    = TestHelper.createStaff()
        val executor = TestHelper.createStaff(email = "restore-member-exec@example.com", role = 2)
        val response = client.patch("/api/staffs/${staff.id}/restore") {
            header(HttpHeaders.Cookie, "staff_id=${TestHelper.signStaffCookie(executor.id)}")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PATCH api staffs id restore deleted admin executor returns 403`() = testApplication {
        application { module(TestHelper.cfg) }
        val staff    = TestHelper.createStaff()
        val executor = TestHelper.createStaff(email = "restore-deleted-admin-exec@example.com", role = 1)
        transaction(TestHelper.db) {
            Staffs.update({ Staffs.id eq executor.id }) {
                it[Staffs.deletedAt] = LocalDateTime.now()
                it[Staffs.updatedAt] = LocalDateTime.now()
            }
        }
        val response = client.patch("/api/staffs/${staff.id}/restore") {
            header(HttpHeaders.Cookie, "staff_id=${TestHelper.signStaffCookie(executor.id)}")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `DELETE api staffs id delete returns 401 when unauthenticated`() = testApplication {
        application { module(TestHelper.cfg) }
        val target = TestHelper.createStaff(email = "target-unauth-destroy@example.com")
        val response = client.delete("/api/staffs/${target.id}/delete")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `DELETE api staffs id delete non-admin executor returns 403`() = testApplication {
        application { module(TestHelper.cfg) }
        val target   = TestHelper.createStaff(email = "target-nonadmin-destroy@example.com")
        val executor = TestHelper.createStaff(email = "destroy-member-exec@example.com", role = 2)
        val response = client.delete("/api/staffs/${target.id}/delete") {
            header(HttpHeaders.Cookie, "staff_id=${TestHelper.signStaffCookie(executor.id)}")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `DELETE api staffs id delete deleted admin executor returns 403`() = testApplication {
        application { module(TestHelper.cfg) }
        val target   = TestHelper.createStaff(email = "target-deletedadmin-destroy@example.com")
        val executor = TestHelper.createStaff(email = "destroy-deleted-admin-exec@example.com", role = 1)
        transaction(TestHelper.db) {
            Staffs.update({ Staffs.id eq executor.id }) {
                it[Staffs.deletedAt] = LocalDateTime.now()
                it[Staffs.updatedAt] = LocalDateTime.now()
            }
        }
        val response = client.delete("/api/staffs/${target.id}/delete") {
            header(HttpHeaders.Cookie, "staff_id=${TestHelper.signStaffCookie(executor.id)}")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `DELETE api staffs id delete soft-deletes staff and returns id`() = testApplication {
        application { module(TestHelper.cfg) }
        val executor = TestHelper.createStaff(email = "exec@example.com")
        val target   = TestHelper.createStaff(email = "target@example.com")
        val response = client.delete("/api/staffs/${target.id}/delete") {
            header(HttpHeaders.Cookie, "staff_id=${TestHelper.signStaffCookie(executor.id)}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(target.id, body["id"]!!.jsonPrimitive.long)
    }
}
