/*
 * クライアント HTTP ハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.handler

import com.authorization.domain.client.JwtHistoryRepository
import com.authorization.infrastructure.mail.Mailer
import com.authorization.support.AppException
import com.authorization.usecase.client.InfoDto
import com.authorization.usecase.client.Interactor as ClientUC
import com.authorization.usecase.client.ListConditionDto
import com.authorization.usecase.client.QrDto
import com.authorization.usecase.client.StartDto
import com.authorization.usecase.client.StopDto
import com.authorization.usecase.client.StoreDto
import com.authorization.usecase.client.UpdateDto
import com.authorization.usecase.notification.FanOutDto
import com.authorization.usecase.notification.Interactor as NotificationUC
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val fmtSec = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private fun LocalDateTime.fmt() = format(fmt)
private fun LocalDateTime.fmtSec() = format(fmtSec)
private fun LocalDateTime?.fmtOrNull(): JsonElement = if (this == null) JsonNull else JsonPrimitive(format(fmt))

/**
 * クライアント API のハンドラーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class ClientHandler(
    private val clientUC: ClientUC,
    private val notificationUC: NotificationUC,
    private val mailer: Mailer,
    private val jwtHistoryRepo: JwtHistoryRepository,
    private val frontendUrl: String,
) {

    /**
     * クライアント一覧を取得します。
     *
     * @param call アプリケーションコール
     */
    suspend fun index(call: ApplicationCall) {
        val keyword   = call.request.queryParameters["keyword"]
        val startFrom = call.request.queryParameters["start_from"]
        val startTo   = call.request.queryParameters["start_to"]
        val clients = clientUC.findByCondition(ListConditionDto(keyword = keyword, startFrom = startFrom, startTo = startTo))
        val list = buildJsonArray {
            clients.forEach { c ->
                add(buildJsonObject {
                    put("id",         c.id)
                    put("name",       c.name)
                    put("status",     c.status)
                    put("start_at",   c.startAt.fmtOrNull())
                    put("stop_at",    c.stopAt.fmtOrNull())
                    put("created_at", c.createdAt.fmt())
                    put("updated_at", c.updatedAt.fmt())
                })
            }
        }
        call.respond(list)
    }

    /**
     * 指定した ID のクライアント詳細を取得します。
     *
     * @param call アプリケーションコール
     */
    suspend fun show(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        val c = clientUC.findById(id)
        call.respond(buildJsonObject {
            put("id",         c.id)
            put("name",       c.name)
            put("identifier", c.identifier)
            put("post_code",  c.postCode)
            put("pref",       c.pref)
            put("city",       c.city)
            put("address",    c.address)
            put("building",   c.building)
            put("tel",        c.tel)
            put("email",      c.email)
            put("status",     c.status)
            put("start_at",   c.startAt.fmtOrNull())
            put("stop_at",    c.stopAt.fmtOrNull())
            put("created_at", c.createdAt.fmt())
            put("updated_at", c.updatedAt.fmt())
        })
    }

    /**
     * クライアントを新規登録します。
     *
     * @param call アプリケーションコール
     */
    suspend fun store(call: ApplicationCall) {
        val executorId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        val body = call.receive<JsonObject>()
        val dto = StoreDto(
            name      = body["name"]?.jsonPrimitive?.content ?: "",
            postCode  = body["post_code"]?.jsonPrimitive?.contentOrNull ?: "",
            pref      = body["pref"]?.jsonPrimitive?.contentOrNull ?: "",
            city      = body["city"]?.jsonPrimitive?.contentOrNull ?: "",
            address   = body["address"]?.jsonPrimitive?.contentOrNull ?: "",
            building  = body["building"]?.jsonPrimitive?.contentOrNull ?: "",
            tel       = body["tel"]?.jsonPrimitive?.contentOrNull ?: "",
            email     = body["email"]?.jsonPrimitive?.contentOrNull ?: "",
            executorId = executorId,
        )
        val client = newSuspendedTransaction {
            val c = clientUC.store(dto)
            notificationUC.fanOut(FanOutDto(
                title       = "新しいクライアントが登録されました",
                message     = c.name,
                messageType = 1,
                executorId  = executorId,
                url         = "/clients/show?id=${c.id}",
            ))
            c
        }
        val activateUrl = "$frontendUrl/clients/${client.identifier}/qr"
        call.application.launch {
            mailer.sendActivation(client.email, client.name, activateUrl)
        }
        call.respond(HttpStatusCode.Created, buildJsonObject { put("id", client.id) })
    }

    /**
     * クライアントを更新します。
     *
     * @param call アプリケーションコール
     */
    suspend fun update(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        val executorId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        val body = call.receive<JsonObject>()
        val dto = UpdateDto(
            id        = id,
            name      = body["name"]?.jsonPrimitive?.contentOrNull,
            postCode  = body["post_code"]?.jsonPrimitive?.contentOrNull,
            pref      = body["pref"]?.jsonPrimitive?.contentOrNull,
            city      = body["city"]?.jsonPrimitive?.contentOrNull,
            address   = body["address"]?.jsonPrimitive?.contentOrNull,
            building  = body["building"]?.jsonPrimitive?.contentOrNull,
            tel       = body["tel"]?.jsonPrimitive?.contentOrNull,
            email     = body["email"]?.jsonPrimitive?.contentOrNull,
            status    = body["status"]?.jsonPrimitive?.intOrNull,
            executorId = executorId,
        )
        val c = newSuspendedTransaction { clientUC.update(dto) }
        call.respond(buildJsonObject {
            put("id",         c.id)
            put("name",       c.name)
            put("identifier", c.identifier)
            put("post_code",  c.postCode)
            put("pref",       c.pref)
            put("city",       c.city)
            put("address",    c.address)
            put("building",   c.building)
            put("tel",        c.tel)
            put("email",      c.email)
            put("status",     c.status)
            put("start_at",   c.startAt.fmtOrNull())
            put("stop_at",    c.stopAt.fmtOrNull())
            put("created_at", c.createdAt.fmt())
            put("updated_at", c.updatedAt.fmt())
        })
    }

    /**
     * スマホ連携用 QR コードデータを返します。
     *
     * @param call アプリケーションコール
     */
    suspend fun qr(call: ApplicationCall) {
        val identifier = call.parameters["identifier"] ?: ""
        try {
            val vo = clientUC.getQr(QrDto(identifier = identifier))
            call.respond(buildJsonObject {
                put("identifier",   vo.identifier)
                put("deeplink_url", vo.deeplinkUrl)
            })
        } catch (e: AppException) {
            call.respond(HttpStatusCode.fromValue(e.statusCode), buildJsonObject { put("error", e.message) })
        }
    }

    /**
     * スマホアプリ向けにクライアント情報を返します。
     *
     * @param call アプリケーションコール
     */
    suspend fun info(call: ApplicationCall) {
        val identifier = call.parameters["identifier"] ?: ""
        try {
            val vo = clientUC.getInfo(InfoDto(identifier = identifier))
            call.respond(buildJsonObject {
                put("identifier", vo.identifier)
                put("name",       vo.name)
                put("status",     vo.status)
            })
        } catch (e: AppException) {
            call.respond(HttpStatusCode.fromValue(e.statusCode), buildJsonObject { put("error", e.message) })
        }
    }

    /**
     * スマホアプリからの利用開始を処理し、アクセストークンを返します。
     *
     * @param call アプリケーションコール
     */
    suspend fun start(call: ApplicationCall) {
        val identifier = call.parameters["identifier"] ?: ""
        try {
            val vo = clientUC.start(StartDto(identifier = identifier))
            call.respond(buildJsonObject {
                put("access_token", vo.accessToken)
            })
        } catch (e: AppException) {
            call.respond(HttpStatusCode.fromValue(e.statusCode), buildJsonObject { put("error", e.message) })
        }
    }

    /**
     * スマホアプリからの利用停止を処理します。
     *
     * @param call アプリケーションコール
     */
    suspend fun stop(call: ApplicationCall) {
        val identifier = call.parameters["identifier"] ?: ""
        try {
            clientUC.stop(StopDto(identifier = identifier))
            call.respond(HttpStatusCode.OK, buildJsonObject {})
        } catch (e: AppException) {
            call.respond(HttpStatusCode.fromValue(e.statusCode), buildJsonObject { put("error", e.message) })
        }
    }

    /**
     * クライアントを論理削除します。
     *
     * @param call アプリケーションコール
     */
    suspend fun destroy(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        val executorId = call.request.cookies["staff_id"]?.toLongOrNull() ?: 0L
        newSuspendedTransaction { clientUC.destroy(id, executorId) }
        call.respond(HttpStatusCode.OK, buildJsonObject {})
    }

    /**
     * 指定したクライアント ID の JWT 履歴一覧を返します。
     *
     * @param call アプリケーションコール
     */
    suspend fun jwtHistories(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, buildJsonObject { put("error", "invalid_id") })
        val histories = jwtHistoryRepo.findByClientId(id)
        val list = buildJsonArray {
            histories.forEach { h ->
                add(buildJsonObject {
                    put("id",       h.id)
                    put("member_id", h.memberId)
                    put("issue_at", h.issueAt.fmtSec())
                    put("jwt",      h.jwt)
                })
            }
        }
        call.respond(list)
    }
}
