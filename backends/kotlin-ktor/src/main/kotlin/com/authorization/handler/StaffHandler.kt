package com.authorization.handler

import com.authorization.domain.staff.Condition
import com.authorization.usecase.staff.DestroyDto
import com.authorization.usecase.staff.Interactor as StaffUC
import com.authorization.usecase.staff.UpdateRoleDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.*
import java.time.format.DateTimeFormatter

private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

class StaffHandler(private val staffUC: StaffUC) {

    suspend fun index(call: ApplicationCall) {
        val keyword = call.request.queryParameters["keyword"]
        val roles = call.request.queryParameters.getAll("roles")
            ?.flatMap { it.split(",") }
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: emptyList()
        val staffs = staffUC.findByCondition(Condition(keyword = keyword, roles = roles))
        val items = buildJsonArray {
            staffs.forEach { s ->
                add(buildJsonObject {
                    put("id",         s.id)
                    put("name",       s.name)
                    put("email",      s.email)
                    put("role",       s.role)
                    put("status",     StaffUC.status(s))
                    put("created_at", s.createdAt.format(fmt))
                    put("updated_at", s.updatedAt.format(fmt))
                })
            }
        }
        call.respond(buildJsonObject { put("items", items) })
    }

    suspend fun updateRole(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        val executorId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        val body = call.receive<JsonObject>()
        val role = body["role"]?.jsonPrimitive?.intOrNull
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "role_required") })
        staffUC.updateRole(UpdateRoleDto(id = id, role = role, executorId = executorId))
        call.respond(buildJsonObject { put("id", id) })
    }

    suspend fun restore(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        staffUC.restore(id)
        call.respond(buildJsonObject { put("id", id) })
    }

    suspend fun destroy(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        val executorId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        staffUC.destroy(DestroyDto(id = id, executorId = executorId))
        call.respond(buildJsonObject { put("id", id) })
    }
}
