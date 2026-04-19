package com.authorization.handler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

object ClientHandler {
    suspend fun index(call: ApplicationCall) = call.respondText("[]", ContentType.Application.Json)
    suspend fun store(call: ApplicationCall) = call.respondText("{}", ContentType.Application.Json)
    suspend fun show(call: ApplicationCall) = call.respondText("{}", ContentType.Application.Json)
    suspend fun update(call: ApplicationCall) = call.respondText("{}", ContentType.Application.Json)
    suspend fun destroy(call: ApplicationCall) = call.respond(HttpStatusCode.NoContent)
}
