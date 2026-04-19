package com.authorization.handler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

object AuthHandler {
    suspend fun googleRedirect(call: ApplicationCall) = call.respond(HttpStatusCode.OK)
    suspend fun googleCallback(call: ApplicationCall) = call.respond(HttpStatusCode.OK)
    suspend fun getMyProfile(call: ApplicationCall) = call.respondText("{}", ContentType.Application.Json)
    suspend fun login(call: ApplicationCall) = call.respondText("{}", ContentType.Application.Json)
    suspend fun logout(call: ApplicationCall) = call.respondText("{}", ContentType.Application.Json)
    suspend fun invitation(call: ApplicationCall) = call.respondText("{}", ContentType.Application.Json)
}
