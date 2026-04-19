package com.authorization.handler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

object StaffHandler {
    suspend fun index(call: ApplicationCall) = call.respondText("[]", ContentType.Application.Json)
    suspend fun updateRole(call: ApplicationCall) = call.respondText("{}", ContentType.Application.Json)
    suspend fun restore(call: ApplicationCall) = call.respondText("{}", ContentType.Application.Json)
    suspend fun destroy(call: ApplicationCall) = call.respond(HttpStatusCode.NoContent)
}
