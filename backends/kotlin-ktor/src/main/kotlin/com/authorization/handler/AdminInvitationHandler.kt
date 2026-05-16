/*
 * 招待管理 HTTP ハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.handler

import com.authorization.usecase.invitation.Interactor as InvitationUC
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * 招待管理 API のハンドラーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class AdminInvitationHandler(private val invitationUC: InvitationUC) {

    /**
     * 現在有効な招待トークンを取得します。
     * クエリパラメータ role（1=管理者、2=メンバー、デフォルト 2）でフィルタします。
     *
     * @param call アプリケーションコール
     */
    suspend fun index(call: ApplicationCall) {
        val role = resolveRole(call) ?: return
        val v = invitationUC.current(role)
        call.respond(buildJsonObject {
            put("found",       true)
            put("url",         v.url)
            put("display_url", v.displayUrl)
            put("token",       v.token)
        })
    }

    /**
     * 招待トークンを新規発行します。
     * クエリパラメータ role（1=管理者、2=メンバー、デフォルト 2）でロールを指定します。
     * 認証クッキーがない場合は 401 を返します。
     *
     * @param call アプリケーションコール
     */
    suspend fun issue(call: ApplicationCall) {
        val staffId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        if (staffId == 0L) {
            call.respond(HttpStatusCode.Unauthorized, buildJsonObject { put("error", "unauthenticated") })
            return
        }
        val role = resolveRole(call) ?: return
        val v = newSuspendedTransaction { invitationUC.issue(role) }
        call.respond(buildJsonObject {
            put("found",       true)
            put("url",         v.url)
            put("display_url", v.displayUrl)
            put("token",       v.token)
        })
    }

    /**
     * クエリパラメータ role を解決します。
     * 未指定の場合は 2（メンバー）を返します。
     * 1 または 2 以外の場合は 400 を返し null を返します。
     */
    private suspend fun resolveRole(call: ApplicationCall): Int? {
        val rawRole = call.request.queryParameters["role"]?.toIntOrNull() ?: 2
        if (rawRole != 1 && rawRole != 2) {
            call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_role") })
            return null
        }
        return rawRole
    }
}
