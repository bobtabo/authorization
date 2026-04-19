package com.authorization.handler

import io.ktor.server.application.*
import io.ktor.server.response.*

object AdminInvitationHandler {
    suspend fun index(call: ApplicationCall) = call.respondText("{}", io.ktor.http.ContentType.Application.Json)
    suspend fun issue(call: ApplicationCall) = call.respondText("{}", io.ktor.http.ContentType.Application.Json)
}
