/*
 * ゲート HTTP ハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.handler

import com.authorization.usecase.gate.Interactor as GateUC
import com.authorization.usecase.gate.IssueDto
import com.authorization.usecase.gate.VerifyDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.*

/**
 * ゲート認可 API のハンドラーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class GateHandler(private val gateUC: GateUC) {

    /**
     * JWT トークンを発行します。
     *
     * @param call アプリケーションコール
     */
    suspend fun issue(call: ApplicationCall) {
        val member = call.request.queryParameters["member"]
        if (member.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "member_required") })
            return
        }
        val auth = call.request.headers["Authorization"] ?: ""
        val accessToken = if (auth.startsWith("Bearer ")) auth.removePrefix("Bearer ") else ""
        val vo = gateUC.issueToken(IssueDto(accessToken = accessToken, memberId = member))
        call.respond(buildJsonObject { put("token", vo.token) })
    }

    /**
     * JWT トークンを検証します。
     *
     * @param call アプリケーションコール
     */
    suspend fun verify(call: ApplicationCall) {
        val identifier = call.parameters["identifier"] ?: ""
        val token = call.request.queryParameters["token"]
        if (token.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "token_required") })
            return
        }
        val vo = gateUC.verify(VerifyDto(identifier = identifier, token = token))
        call.respond(buildJsonObject {
            vo.claims.forEach { (k, v) ->
                when (v) {
                    null       -> put(k, JsonNull)
                    is Boolean -> put(k, v)
                    is Long    -> put(k, v)
                    is Double  -> put(k, v)
                    else       -> put(k, v.toString())
                }
            }
        })
    }
}
