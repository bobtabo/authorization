package com.authorization

import com.authorization.config.ConfigLoader
import com.authorization.handler.*
import com.authorization.infrastructure.cache.RedisGateRepository
import com.authorization.infrastructure.cache.newRedisPool
import com.authorization.infrastructure.db.initDatabase
import com.authorization.infrastructure.mail.Mailer
import com.authorization.infrastructure.persistence.*
import com.authorization.usecase.auth.Interactor as AuthInteractor
import com.authorization.usecase.client.Interactor as ClientInteractor
import com.authorization.usecase.gate.Interactor as GateInteractor
import com.authorization.usecase.invitation.Interactor as InvitationInteractor
import com.authorization.usecase.notification.Interactor as NotificationInteractor
import com.authorization.usecase.staff.Interactor as StaffInteractor
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val cfg = ConfigLoader.load()

    val db         = initDatabase(cfg)
    val redisPool  = newRedisPool(cfg)

    val clientRepo       = ExposedClientRepository(db)
    val staffRepo        = ExposedStaffRepository(db)
    val invitationRepo   = ExposedInvitationRepository(db, cfg)
    val notificationRepo = ExposedNotificationRepository(db)
    val gateCache        = RedisGateRepository(redisPool, cfg)

    val authUC         = AuthInteractor(staffRepo)
    val clientUC       = ClientInteractor(clientRepo)
    val staffUC        = StaffInteractor(staffRepo)
    val invitationUC   = InvitationInteractor(invitationRepo)
    val gateUC         = GateInteractor(clientRepo, gateCache, cfg)
    val notificationUC = NotificationInteractor(notificationRepo, staffRepo)
    val mailer         = Mailer(cfg.mail)

    val authH         = AuthHandler(authUC, invitationUC, cfg)
    val clientH       = ClientHandler(clientUC, notificationUC, mailer)
    val staffH        = StaffHandler(staffUC)
    val adminInvH     = AdminInvitationHandler(invitationUC)
    val gateH         = GateHandler(gateUC)
    val notificationH = NotificationHandler(notificationUC, cfg)

    install(ContentNegotiation) { json() }

    routing {
        // OAuth（ブラウザリダイレクトのため /api 外）
        get("/auth/google/redirect") { authH.googleRedirect(call) }
        get("/auth/google/callback") { authH.googleCallback(call) }

        route("/api") {
            // --- auth ---
            get("/auth/me")                { authH.getMyProfile(call) }
            get("/auth/login")             { authH.login(call) }
            get("/auth/logout")            { authH.logout(call) }
            get("/auth/invitation/{token}") { authH.invitation(call) }

            // --- clients ---
            get("/clients")              { clientH.index(call) }
            post("/clients/store")       { clientH.store(call) }
            put("/clients/{id}/update")  { clientH.update(call) }
            get("/clients/{id}")         { clientH.show(call) }
            delete("/clients/{id}/delete") { clientH.destroy(call) }

            // --- staffs ---
            get("/staffs")                    { staffH.index(call) }
            patch("/staffs/{id}/updateRole")  { staffH.updateRole(call) }
            patch("/staffs/{id}/restore")     { staffH.restore(call) }
            delete("/staffs/{id}/delete")     { staffH.destroy(call) }

            // --- admin ---
            get("/admin/invitation")       { adminInvH.index(call) }
            get("/admin/invitation/issue") { adminInvH.issue(call) }

            // --- gate ---
            get("/gate/issue")                      { gateH.issue(call) }
            get("/gate/client/{identifier}/verify") { gateH.verify(call) }

            // --- notifications ---
            get("/notifications/counts") { notificationH.counts(call) }
            get("/notifications")        { notificationH.index(call) }
            patch("/notifications")      { notificationH.readAll(call) }
            patch("/notifications/{id}") { notificationH.read(call) }
        }
    }
}
