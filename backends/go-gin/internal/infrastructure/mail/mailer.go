package mail

import (
	"authorization-go/internal/config"
	"fmt"
	"log"
	"mime"
	"net/smtp"
	"strings"
	"time"
)

type Mailer struct {
	cfg config.MailConfig
}

func NewMailer(cfg config.MailConfig) *Mailer {
	return &Mailer{cfg: cfg}
}

func envLabel(env string) string {
	switch env {
	case "local":
		return "Local"
	case "testing":
		return "Test"
	case "develop":
		return "Develop"
	case "staging":
		return "Staging"
	default:
		return ""
	}
}

func mailSubject(env, subject string) string {
	if label := envLabel(env); label != "" {
		return "[" + label + "]" + subject
	}
	return subject
}

func (m *Mailer) SendAccessToken(to, clientName, token string) {
	if to == "" {
		return
	}
	subject := mailSubject(m.cfg.AppEnv, fmt.Sprintf("【%s】アクセストークンのお知らせ", m.cfg.AppName))
	body := buildAccessTokenHTML(clientName, token, m.cfg.AppName)

	fromHeader := mime.QEncoding.Encode("UTF-8", m.cfg.AppName) + " <" + m.cfg.FromAddress + ">"
	msg := "MIME-Version: 1.0\r\n" +
		"Content-Type: text/html; charset=UTF-8\r\n" +
		"From: " + fromHeader + "\r\n" +
		"To: " + to + "\r\n" +
		"Subject: " + mime.QEncoding.Encode("UTF-8", subject) + "\r\n" +
		"\r\n" + body

	addr := m.cfg.Host + ":" + m.cfg.Port
	var auth smtp.Auth
	if m.cfg.Username != "" {
		auth = smtp.PlainAuth("", m.cfg.Username, m.cfg.Password, m.cfg.Host)
	}
	if err := smtp.SendMail(addr, auth, m.cfg.FromAddress, []string{to}, []byte(msg)); err != nil {
		log.Printf("mail send error: %v", err)
	}
}

func buildAccessTokenHTML(name, token, appName string) string {
	year := time.Now().Year()
	html := accessTokenTemplate
	html = strings.ReplaceAll(html, "{{NAME}}", name)
	html = strings.ReplaceAll(html, "{{TOKEN}}", token)
	html = strings.ReplaceAll(html, "{{APP_NAME}}", appName)
	html = strings.ReplaceAll(html, "{{YEAR}}", fmt.Sprintf("%d", year))
	return html
}

const accessTokenTemplate = `<!DOCTYPE html>
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
</html>`
