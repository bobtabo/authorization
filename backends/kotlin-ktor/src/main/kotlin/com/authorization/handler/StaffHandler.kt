/*
 * スタッフ HTTP ハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
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
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.format.DateTimeFormatter

private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

/**
 * スタッフ API のハンドラーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class StaffHandler(private val staffUC: StaffUC) {

    /**
     * スタッフ一覧を取得します。
     *
     * @param call アプリケーションコール
     */
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
                    put("status",     s.status)
                    put("created_at", s.createdAt.format(fmt))
                    put("updated_at", s.updatedAt.format(fmt))
                })
            }
        }
        call.respond(buildJsonObject { put("items", items) })
    }

    /**
     * スタッフのロールを更新します。
     *
     * @param call アプリケーションコール
     */
    suspend fun updateRole(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        val executorId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        val body = call.receive<JsonObject>()
        val role = body["role"]?.jsonPrimitive?.intOrNull
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "role_required") })
        newSuspendedTransaction { staffUC.updateRole(UpdateRoleDto(id = id, role = role, executorId = executorId)) }
        call.respond(buildJsonObject { put("id", id) })
    }

    /**
     * 論理削除されたスタッフを復元します。
     *
     * @param call アプリケーションコール
     */
    suspend fun restore(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        newSuspendedTransaction { staffUC.restore(id) }
        call.respond(buildJsonObject { put("id", id) })
    }

    /**
     * スタッフを論理削除します。
     *
     * @param call アプリケーションコール
     */
    suspend fun destroy(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        val executorId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        newSuspendedTransaction { staffUC.destroy(DestroyDto(id = id, executorId = executorId)) }
        call.respond(buildJsonObject { put("id", id) })
    }
}
