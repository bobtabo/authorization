/*
 * メール送信モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.mail

import com.authorization.config.MailConfig
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.time.Year
import java.util.Properties

/**
 * メール送信クラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Mailer(private val cfg: MailConfig) {

    /**
     * クライアントへアクセストークン通知メールを送信します。
     *
     * @param to 宛先メールアドレス
     * @param clientName クライアント名
     * @param token アクセストークン
     */
    fun sendAccessToken(to: String, clientName: String, token: String) {
        if (to.isBlank()) return

        val subject = mailSubject("【${cfg.appName} / Kotlin】アクセストークンのお知らせ")
        val body = buildAccessTokenHtml(clientName, token, cfg.appName)

        val props = Properties().apply {
            put("mail.smtp.host", cfg.host)
            put("mail.smtp.port", cfg.port)
            put("mail.smtp.auth", cfg.username.isNotBlank().toString())
        }

        val session = if (cfg.username.isNotBlank()) {
            Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication() =
                    PasswordAuthentication(cfg.username, cfg.password)
            })
        } else {
            Session.getInstance(props)
        }

        try {
            val msg = MimeMessage(session).apply {
                setFrom(InternetAddress(cfg.fromAddress, cfg.appName, "UTF-8"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject, "UTF-8")
                setContent(body, "text/html; charset=UTF-8")
            }
            Transport.send(msg)
        } catch (e: Exception) {
            System.err.println("mail send error: ${e.message}")
        }
    }

    /**
     * 環境に応じたメール件名プレフィックスを付与します。
     *
     * @param subject 件名
     * @return プレフィックス付き件名
     */
    private fun mailSubject(subject: String): String {
        val label = when (cfg.appEnv) {
            "local"   -> "Local"
            "testing" -> "Test"
            "develop" -> "Develop"
            "staging" -> "Staging"
            else      -> ""
        }
        return if (label.isNotEmpty()) "[$label]$subject" else subject
    }
}

/**
 * アクセストークン通知メールの HTML を生成します。
 *
 * @param name クライアント名
 * @param token アクセストークン
 * @param appName アプリケーション名
 * @return HTML 文字列
 */
private fun buildAccessTokenHtml(name: String, token: String, appName: String): String {
    val year = Year.now().value
    return ACCESS_TOKEN_TEMPLATE
        .replace("{{NAME}}", name)
        .replace("{{TOKEN}}", token)
        .replace("{{APP_NAME}}", appName)
        .replace("{{YEAR}}", year.toString())
}

private val ACCESS_TOKEN_TEMPLATE = """<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{{APP_NAME}} — アクセストークン</title>
</head>
<body style="margin:0;padding:0;background-color:#f9fafb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f9fafb;">
<tr><td align="center" style="padding:40px 16px;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="max-width:560px;margin:0 auto;">
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-bottom:none;border-radius:12px 12px 0 0;padding:20px 24px;">
<span style="font-size:15px;font-weight:600;color:#1f2937;">{{APP_NAME}}</span>
</td></tr>
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-top:1px solid #f3f4f6;padding:28px 24px 32px;border-radius:0 0 12px 12px;">
<h1 style="margin:0 0 8px;font-size:18px;font-weight:600;color:#111827;">アクセストークンを発行しました</h1>
<p style="margin:0 0 20px;font-size:14px;color:#6b7280;">{{NAME}} 様</p>
<p style="margin:0 0 24px;font-size:14px;line-height:1.75;color:#374151;">
ご利用の <strong style="color:#4f46e5;">{{APP_NAME}}</strong> 向けにアクセストークンを発行しました。<br>
以下のトークンは第三者に共有せず、安全な場所で保管してください。
</p>
<p style="margin:0 0 8px;font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;letter-spacing:0.06em;">発行されたトークン</p>
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f3f4f6;border:1px solid #e5e7eb;border-radius:8px;">
<tr><td style="padding:16px 18px;word-break:break-all;">
<code style="font-family:monospace;font-size:13px;color:#111827;">{{TOKEN}}</code>
</td></tr>
</table>
<p style="margin:20px 0 0;font-size:12px;color:#9ca3af;">このメールに心当たりがない場合は、管理者までご連絡ください。</p>
</td></tr>
<tr><td align="center" style="padding:24px 16px 8px;">
<p style="margin:0;font-size:11px;color:#9ca3af;">© {{YEAR}} {{APP_NAME}}</p>
</td></tr>
</table>
</td></tr>
</table>
</body>
</html>"""
