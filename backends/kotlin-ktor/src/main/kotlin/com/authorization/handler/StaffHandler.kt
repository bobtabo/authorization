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

private const val DEFAULT_PAGE_COUNT = 5

private fun buildPager(count: Int, limit: Int, offset: Int, recordCount: Int): JsonObject {
    val effectiveLimit = if (limit <= 0) 10 else limit
    val pageCount = maxOf(1, kotlin.math.ceil(count.toDouble() / effectiveLimit).toInt())
    val lastPageOffset = (pageCount * effectiveLimit) - effectiveLimit
    val effectiveOffset = if (count > 0 && offset > lastPageOffset) lastPageOffset else offset
    val page = (kotlin.math.ceil(effectiveOffset.toDouble() / effectiveLimit).toInt()) + 1
    val startPage = maxOf(1, page - (DEFAULT_PAGE_COUNT - 1))
    val endPage = minOf(pageCount, startPage + (DEFAULT_PAGE_COUNT - 1))
    return buildJsonObject {
        put("count", count)
        put("limit", effectiveLimit)
        put("next", pageCount > page)
        put("previous", page > 1)
        put("page", page)
        put("nextPage", page + 1)
        put("previousPage", page - 1)
        put("pageCount", pageCount)
        put("first", page > 1)
        put("last", pageCount > page)
        put("firstRecordCount", effectiveOffset + 1)
        put("lastRecordCount", effectiveOffset + recordCount)
        put("startPage", startPage)
        put("endPage", endPage)
    }
}

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
        val keyword  = call.request.queryParameters["keyword"]
        val roles    = call.request.queryParameters.getAll("roles")
            ?.flatMap { it.split(",") }
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: emptyList()
        val sort     = call.request.queryParameters["sort"]
        val sortType = call.request.queryParameters["sort_type"]
        val limit    = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceAtLeast(1) ?: 10
        val page     = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val offset   = limit * (page - 1)

        val cond = Condition(keyword = keyword, roles = roles, offset = offset, limit = limit, sort = sort, sortType = sortType)
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
