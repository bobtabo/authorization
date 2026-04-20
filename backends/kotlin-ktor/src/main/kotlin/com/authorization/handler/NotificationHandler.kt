package com.authorization.handler

import com.authorization.config.Config
import com.authorization.usecase.notification.Interactor as NotificationUC
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.*

class NotificationHandler(
    private val notificationUC: NotificationUC,
    private val cfg: Config,
) {
    suspend fun counts(call: ApplicationCall) {
        val staffId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        if (staffId == 0L) {
            call.respond(HttpStatusCode.Unauthorized, buildJsonObject { put("error", "unauthenticated") })
            return
        }
        val (unread, total) = notificationUC.counts(staffId)
        call.respond(buildJsonObject {
            put("unread", unread)
            put("total",  total)
        })
    }

    suspend fun index(call: ApplicationCall) {
        val staffId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        if (staffId == 0L) {
            call.respond(HttpStatusCode.Unauthorized, buildJsonObject { put("error", "unauthenticated") })
            return
        }
        val cursor = call.request.queryParameters["cursor"]
        val limit  = call.request.queryParameters["limit"]?.toLongOrNull()
            ?: cfg.app.notificationDefaultLimit
        val page = notificationUC.listPage(staffId, cursor, limit)
        val items = buildJsonArray {
            page.items.forEach { n ->
                add(NotificationUC.mapNotification(n).let { m ->
                    buildJsonObject {
                        m.forEach { (k, v) ->
                            when (v) {
                                null         -> put(k, JsonNull)
                                is Boolean   -> put(k, v)
                                is Long      -> put(k, v)
                                is Int       -> put(k, v)
                                else         -> put(k, v.toString())
                            }
                        }
                    }
                })
            }
        }
        call.respond(buildJsonObject {
            put("items",       items)
            put("next_cursor", page.nextCursor?.let { JsonPrimitive(it) } ?: JsonNull)
        })
    }

    suspend fun readAll(call: ApplicationCall) {
        val staffId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        if (staffId == 0L) {
            call.respond(HttpStatusCode.Unauthorized, buildJsonObject { put("error", "unauthenticated") })
            return
        }
        val updated = notificationUC.bulkMarkRead(staffId)
        call.respond(buildJsonObject { put("updated", updated) })
    }

    suspend fun read(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        notificationUC.markRead(id)
        call.respond(buildJsonObject { put("id", id) })
    }
}
