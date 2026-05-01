/*
 * 招待管理 HTTP ハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.handler

import com.authorization.usecase.invitation.Interactor as InvitationUC
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
     *
     * @param call アプリケーションコール
     */
    suspend fun index(call: ApplicationCall) {
        val v = invitationUC.current()
        call.respond(buildJsonObject {
            put("found",       true)
            put("url",         v.url)
            put("display_url", v.displayUrl)
            put("token",       v.token)
        })
    }

    /**
     * 招待トークンを新規発行します。
     *
     * @param call アプリケーションコール
     */
    suspend fun issue(call: ApplicationCall) {
        val v = newSuspendedTransaction { invitationUC.issue() }
        call.respond(buildJsonObject {
            put("found",       true)
            put("url",         v.url)
            put("display_url", v.displayUrl)
            put("token",       v.token)
        })
    }
}
