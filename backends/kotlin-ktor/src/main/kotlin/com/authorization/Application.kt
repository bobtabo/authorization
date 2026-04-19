package com.authorization

import com.authorization.handler.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    routing {
        // OAuth（ブラウザリダイレクトのため /api 外）
        get("/auth/google/redirect") { AuthHandler.googleRedirect(call) }
        get("/auth/google/callback") { AuthHandler.googleCallback(call) }

        route("/api") {
            // --- auth ---
            get("/auth/me") { AuthHandler.getMyProfile(call) }
            get("/auth/login") { AuthHandler.login(call) }
            get("/auth/logout") { AuthHandler.logout(call) }
            get("/auth/invitation/{token}") { AuthHandler.invitation(call) }

            // --- clients ---
            get("/clients") { ClientHandler.index(call) }
            post("/clients/store") { ClientHandler.store(call) }
            put("/clients/{id}/update") { ClientHandler.update(call) }
            get("/clients/{id}") { ClientHandler.show(call) }
            delete("/clients/{id}/delete") { ClientHandler.destroy(call) }

            // --- staffs ---
            get("/staffs") { StaffHandler.index(call) }
            patch("/staffs/{id}/updateRole") { StaffHandler.updateRole(call) }
            patch("/staffs/{id}/restore") { StaffHandler.restore(call) }
            delete("/staffs/{id}/delete") { StaffHandler.destroy(call) }

            // --- admin ---
            get("/admin/invitation") { AdminInvitationHandler.index(call) }
            get("/admin/invitation/issue") { AdminInvitationHandler.issue(call) }

            // --- gate ---
            get("/gate/issue") { GateHandler.issue(call) }
            get("/gate/client/{identifier}/verify") { GateHandler.verify(call) }

            // --- notifications ---
            get("/notifications/counts") { NotificationHandler.counts(call) }
            get("/notifications") { NotificationHandler.index(call) }
            patch("/notifications") { NotificationHandler.readAll(call) }
            patch("/notifications/{id}") { NotificationHandler.read(call) }
        }
    }
}
