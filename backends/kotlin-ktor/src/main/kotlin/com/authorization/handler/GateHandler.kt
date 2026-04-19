package com.authorization.handler

import io.ktor.server.application.*
import io.ktor.server.response.*

object GateHandler {
    suspend fun issue(call: ApplicationCall) = call.respondText("{}", io.ktor.http.ContentType.Application.Json)
    suspend fun verify(call: ApplicationCall) = call.respondText("{}", io.ktor.http.ContentType.Application.Json)
}
