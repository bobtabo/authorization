/*
 * スタッフ HTTP ハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.handler

import com.authorization.domain.staff.Condition
import com.authorization.support.AppException
import com.authorization.usecase.staff.DestroyDto
import com.authorization.usecase.staff.Interactor as StaffUC
import com.authorization.usecase.staff.UpdateRoleDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.format.DateTimeFormatter

private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

/**
 * トランザクション内で [block] を実行し、成功時は [onId] のJSONを応答します。
 * [AppException] が発生した場合はそのステータスコードで応答します。
 *
 * newSuspendedTransaction は内部で async{}.await() を使うため、失敗時に await() で
 * 例外を受け取れても、通常のtry/catchでは周囲のコルーチンスコープがキャンセル状態のまま
 * 残ってしまい、後続の call.respond() が CancellationException で失敗する。
 * supervisorScope で失敗の伝播を子コルーチンに閉じ込めることでこれを回避する。
 */
private suspend fun ApplicationCall.respondOrAppError(onId: Long, block: suspend () -> Unit) {
    val result = supervisorScope {
        runCatching { newSuspendedTransaction { block() } }
    }
    result.fold(
        onSuccess = { respond(buildJsonObject { put("id", onId) }) },
        onFailure = { e ->
            if (e is AppException) {
                respond(HttpStatusCode.fromValue(e.statusCode), buildJsonObject { put("error", e.message) })
            } else {
                throw e
            }
        },
    )
}

/**
 * スタッフ API のハンドラーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class StaffHandler(private val staffUC: StaffUC, private val cookieSecret: String) {

    /**
     * スタッフ一覧を取得します。
     *
     * @param call アプリケーションコール
     */
    suspend fun index(call: ApplicationCall) {
        val keyword  = call.request.queryParameters["keyword"]
        val roles    = call.request.queryParameters.getAll("roles")
            ?.flatMap { it.split(",") }
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: emptyList()
        val statuses = call.request.queryParameters.getAll("statuses")
            ?.flatMap { it.split(",") }
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: emptyList()
        val sort     = call.request.queryParameters["sort"]
        val sortType = call.request.queryParameters["sort_type"]
        val limit    = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceAtLeast(1) ?: 10
        val page     = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val offset   = limit * (page - 1)

        val cond = Condition(keyword = keyword, roles = roles, statuses = statuses, offset = offset, limit = limit, sort = sort, sortType = sortType)
        val (staffs, count) = staffUC.findByConditionWithCount(cond)
        val pager = buildPager(count, limit, offset, staffs.size)

        val data = buildJsonArray {
            staffs.forEach { s ->
                add(buildJsonObject {
                    put("id",         s.id)
                    put("name",       s.name)
                    put("email",      s.email)
                    put("role",       s.role)
                    put("status",     s.status)
                    put("version",    s.version)
                    put("created_at", s.createdAt.format(fmt))
                    put("updated_at", s.updatedAt.format(fmt))
                })
            }
        }
        call.respond(buildJsonObject {
            put("data", data)
            put("pager", pager)
        })
    }

    /**
     * スタッフのロールを更新します。
     *
     * @param call アプリケーションコール
     */
    suspend fun updateRole(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        val executorId = verifyStaffId(call.request.cookies["staff_id"], cookieSecret)
        val body = call.receive<JsonObject>()
        val role = body["role"]?.jsonPrimitive?.intOrNull
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "role_required") })
        call.respondOrAppError(id) { staffUC.updateRole(UpdateRoleDto(id = id, role = role, executorId = executorId)) }
    }

    /**
     * 論理削除されたスタッフを復元します。
     *
     * @param call アプリケーションコール
     */
    suspend fun restore(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        call.respondOrAppError(id) { staffUC.restore(id) }
    }

    /**
     * スタッフを論理削除します。
     *
     * @param call アプリケーションコール
     */
    suspend fun destroy(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        val executorId = verifyStaffId(call.request.cookies["staff_id"], cookieSecret)
        call.respondOrAppError(id) { staffUC.destroy(DestroyDto(id = id, executorId = executorId)) }
    }
}
