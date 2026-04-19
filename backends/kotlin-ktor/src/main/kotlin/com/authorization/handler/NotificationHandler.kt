package com.authorization.handler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

object NotificationHandler {
    suspend fun counts(call: ApplicationCall) = call.respondText("{}", ContentType.Application.Json)
    suspend fun index(call: ApplicationCall) = call.respondText("[]", ContentType.Application.Json)
    suspend fun readAll(call: ApplicationCall) = call.respond(HttpStatusCode.NoContent)
    suspend fun read(call: ApplicationCall) = call.respond(HttpStatusCode.NoContent)
}
