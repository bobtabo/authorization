/*
 * 認証 HTTP ハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.handler

import com.authorization.config.Config
import com.authorization.config.OAuthConfig
import com.authorization.usecase.auth.Interactor as AuthUC
import com.authorization.usecase.auth.LoginDto
import com.authorization.usecase.invitation.FindByTokenDto
import com.authorization.usecase.invitation.Interactor as InvitationUC
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * 認証 API のハンドラーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class AuthHandler(
    private val authUC: AuthUC,
    private val invitationUC: InvitationUC,
    private val cfg: Config,
) {

    /**
     * Google OAuth 認証画面へリダイレクトします。
     *
     * @param call アプリケーションコール
     */
    suspend fun googleRedirect(call: ApplicationCall) {
        val url = "https://accounts.google.com/o/oauth2/auth" +
            "?client_id=${cfg.oauth.googleClientId}" +
            "&redirect_uri=${cfg.oauth.googleRedirectUrl}" +
            "&response_type=code&scope=email+profile&access_type=online&state=state"
        call.respondRedirect(url, permanent = false)
    }

    /**
     * Google OAuth コールバックを処理します。
     *
     * @param call アプリケーションコール
     */
    suspend fun googleCallback(call: ApplicationCall) {
        val code = call.request.queryParameters["code"]
        if (code.isNullOrEmpty()) {
            call.respondRedirect(cfg.app.frontendUrl + "/error?code=500", permanent = false)
            return
        }

        val accessToken = try {
            exchangeCodeForToken(code, cfg.oauth)
        } catch (e: Exception) {
            call.respondRedirect(cfg.app.frontendUrl + "/error?code=500", permanent = false)
            return
        }

        val userInfo = try {
            fetchGoogleUserInfo(accessToken)
        } catch (e: Exception) {
            call.respondRedirect(cfg.app.frontendUrl + "/error?code=500", permanent = false)
            return
        }

        val dto = LoginDto(
            provider   = 1,
            providerId = userInfo["id"] ?: "",
            name       = userInfo["name"] ?: "",
            email      = userInfo["email"] ?: "",
            avatar     = userInfo["picture"]?.ifEmpty { null },
        )
        val staff = try {
            authUC.login(dto)
        } catch (e: Exception) {
            call.respondRedirect(cfg.app.frontendUrl + "/error?code=500", permanent = false)
            return
        }

        val secure = cfg.app.env == "production"
        val maxAge = (cfg.app.staffCookieLifetime * 60).toInt()
        call.response.cookies.append(
            Cookie(name = "staff_id", value = staff.id.toString(), maxAge = maxAge,
                   path = "/", secure = secure, httpOnly = true)
        )
        call.respondRedirect(cfg.app.frontendUrl + "/clients", permanent = false)
    }

    /**
     * ログイン中スタッフのプロフィールを取得します。
     *
     * @param call アプリケーションコール
     */
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

    /**
     * ログインを処理します。
     *
     * @param call アプリケーションコール
     */
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

    /**
     * ログアウトを処理します。
     *
     * @param call アプリケーションコール
     */
    suspend fun logout(call: ApplicationCall) {
        call.respond(HttpStatusCode.OK, buildJsonObject {})
    }

    private suspend fun exchangeCodeForToken(code: String, oauth: OAuthConfig): String =
        withContext(Dispatchers.IO) {
            val body = mapOf(
                "code"          to code,
                "client_id"     to oauth.googleClientId,
                "client_secret" to oauth.googleClientSecret,
                "redirect_uri"  to oauth.googleRedirectUrl,
                "grant_type"    to "authorization_code",
            ).entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }

            val conn = URL("https://oauth2.googleapis.com/token").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.outputStream.use { it.write(body.toByteArray()) }

            val response = conn.inputStream.reader().readText()
            Json.parseToJsonElement(response).jsonObject["access_token"]
                ?.jsonPrimitive?.content ?: error("no access_token")
        }

    private suspend fun fetchGoogleUserInfo(accessToken: String): Map<String, String> =
        withContext(Dispatchers.IO) {
            val conn = URL("https://www.googleapis.com/oauth2/v2/userinfo").openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $accessToken")

            val response = conn.inputStream.reader().readText()
            val json = Json.parseToJsonElement(response).jsonObject
            mapOf(
                "id"      to (json["id"]?.jsonPrimitive?.content ?: ""),
                "name"    to (json["name"]?.jsonPrimitive?.content ?: ""),
                "email"   to (json["email"]?.jsonPrimitive?.content ?: ""),
                "picture" to (json["picture"]?.jsonPrimitive?.content ?: ""),
            )
        }

    /**
     * 招待トークンを検証して招待情報を返します。
     *
     * @param call アプリケーションコール
     */
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
