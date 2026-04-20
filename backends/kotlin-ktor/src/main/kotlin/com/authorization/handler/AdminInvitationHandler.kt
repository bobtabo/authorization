package com.authorization.handler

import com.authorization.usecase.invitation.Interactor as InvitationUC
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.*

class AdminInvitationHandler(private val invitationUC: InvitationUC) {

    suspend fun index(call: ApplicationCall) {
        val v = invitationUC.current()
        call.respond(buildJsonObject {
            put("found",       true)
            put("url",         v.url)
            put("display_url", v.displayUrl)
            put("token",       v.token)
        })
    }

    suspend fun issue(call: ApplicationCall) {
        val v = invitationUC.issue()
        call.respond(buildJsonObject {
            put("found",       true)
            put("url",         v.url)
            put("display_url", v.displayUrl)
            put("token",       v.token)
        })
    }
}
