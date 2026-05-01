/*
 * 通知 HTTP ハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.handler

import com.authorization.config.Config
import com.authorization.usecase.notification.Interactor as NotificationUC
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * 通知 API のハンドラーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class NotificationHandler(
    private val notificationUC: NotificationUC,
    private val cfg: Config,
) {

    /**
     * 通知の未読件数と総件数を取得します。
     *
     * @param call アプリケーションコール
     */
    suspend fun counts(call: ApplicationCall) {
        val staffId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        if (staffId == 0L) {
            call.respond(HttpStatusCode.Unauthorized, buildJsonObject { put("error", "unauthenticated") })
            return
        }
        val vo = notificationUC.counts(staffId)
        call.respond(buildJsonObject {
            put("unread", vo.unread)
            put("total",  vo.total)
        })
    }

    /**
     * 通知一覧をページング取得します。
     *
     * @param call アプリケーションコール
     */
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

    /**
     * スタッフの通知をすべて既読にします。
     *
     * @param call アプリケーションコール
     */
    suspend fun readAll(call: ApplicationCall) {
        val staffId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        if (staffId == 0L) {
            call.respond(HttpStatusCode.Unauthorized, buildJsonObject { put("error", "unauthenticated") })
            return
        }
        newSuspendedTransaction { notificationUC.bulkMarkRead(staffId) }
        call.respond(buildJsonObject {})
    }

    /**
     * 指定した通知を既読にします。
     *
     * @param call アプリケーションコール
     */
    suspend fun read(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        newSuspendedTransaction { notificationUC.markRead(id) }
        call.respond(buildJsonObject { put("id", id) })
    }
}
