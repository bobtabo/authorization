package com.authorization.handler

import com.authorization.config.Config
import com.authorization.usecase.auth.Interactor as AuthUC
import com.authorization.usecase.invitation.FindByTokenDto
import com.authorization.usecase.invitation.Interactor as InvitationUC
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.*

class AuthHandler(
    private val authUC: AuthUC,
    private val invitationUC: InvitationUC,
    private val cfg: Config,
) {
    suspend fun googleRedirect(call: ApplicationCall) {
        val url = "https://accounts.google.com/o/oauth2/auth" +
            "?client_id=${cfg.oauth.googleClientId}" +
            "&redirect_uri=${cfg.oauth.googleRedirectUrl}" +
            "&response_type=code&scope=email+profile&access_type=online&state=state"
        call.respondRedirect(url, permanent = false)
    }

    suspend fun googleCallback(call: ApplicationCall) {
        call.respond(HttpStatusCode.OK)
    }

    suspend fun getMyProfile(call: ApplicationCall) {
        val staffId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        if (staffId == 0L) {
            call.respond(HttpStatusCode.Unauthorized, buildJsonObject { put("error", "unauthenticated") })
            return
        }
        val s = authUC.findUser(staffId)
        call.respond(buildJsonObject {
            put("staff_id", s.id)
            put("name",     s.name)
            put("avatar",   s.avatar?.let { JsonPrimitive(it) } ?: JsonNull)
            put("role",     s.role)
        })
    }

    suspend fun login(call: ApplicationCall) {
        val staffId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        if (staffId == 0L) {
            call.respond(HttpStatusCode.Unauthorized, buildJsonObject { put("error", "unauthenticated") })
            return
        }
        val s = authUC.findUser(staffId)
        call.respond(buildJsonObject {
            put("staff_id", s.id)
            put("name",     s.name)
            put("avatar",   s.avatar?.let { JsonPrimitive(it) } ?: JsonNull)
            put("role",     s.role)
        })
    }

    suspend fun logout(call: ApplicationCall) {
        call.respond(HttpStatusCode.OK, buildJsonObject {})
    }

    suspend fun invitation(call: ApplicationCall) {
        val token = call.parameters["token"] ?: ""
        val v = invitationUC.findByToken(FindByTokenDto(token))
        call.respond(buildJsonObject {
            put("found",       true)
            put("url",         v.url)
            put("display_url", v.displayUrl)
            put("token",       v.token)
        })
    }
}
