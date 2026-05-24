package mail

import (
	"authorization-go-echo/internal/config"
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

func (m *Mailer) SendActivation(to, clientName, activateURL string) {
	if to == "" {
		return
	}
	subject := mailSubject(m.cfg.AppEnv, fmt.Sprintf("【%s / Echo】ご利用開始のご案内", m.cfg.AppName))
	body := buildActivationHTML(clientName, activateURL, m.cfg.AppName)

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

func buildActivationHTML(name, activateURL, appName string) string {
	year := time.Now().Year()
	html := activationTemplate
	html = strings.ReplaceAll(html, "{{NAME}}", name)
	html = strings.ReplaceAll(html, "{{ACTIVATE_URL}}", activateURL)
	html = strings.ReplaceAll(html, "{{APP_NAME}}", appName)
	html = strings.ReplaceAll(html, "{{YEAR}}", fmt.Sprintf("%d", year))
	return html
}

const activationTemplate = `<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{{APP_NAME}} — ご利用開始のご案内</title>
</head>
<body style="margin:0;padding:0;background-color:#f9fafb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,'Noto Sans JP','Hiragino Sans','Hiragino Kaku Gothic ProN',Meiryo,sans-serif;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f9fafb;">
<tr><td align="center" style="padding:40px 16px;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="max-width:560px;margin:0 auto;">
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-bottom:none;border-radius:12px 12px 0 0;padding:20px 24px;">
<span style="font-size:15px;font-weight:600;color:#1f2937;">{{APP_NAME}}</span>
</td></tr>
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-top:1px solid #f3f4f6;padding:28px 24px 32px;border-radius:0 0 12px 12px;">
<h1 style="margin:0 0 8px;font-size:18px;font-weight:600;color:#111827;line-height:1.4;letter-spacing:-0.02em;">ご利用開始のご案内</h1>
<p style="margin:0 0 20px;font-size:14px;line-height:1.7;color:#6b7280;">{{NAME}} 様</p>
<p style="margin:0 0 24px;font-size:14px;line-height:1.75;color:#374151;">
<strong style="color:#4f46e5;font-weight:600;">{{APP_NAME}}</strong> へのご登録が完了しました。<br>
以下のボタンからご利用を開始してください。
</p>
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%">
<tr><td align="center" style="padding:8px 0 24px;">
<a href="{{ACTIVATE_URL}}" style="display:inline-block;background-color:#4f46e5;color:#ffffff;font-size:14px;font-weight:600;text-decoration:none;padding:12px 32px;border-radius:8px;letter-spacing:0.01em;">ご利用を開始する</a>
</td></tr>
</table>
<p style="margin:0 0 8px;font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;letter-spacing:0.06em;">ボタンが開かない場合</p>
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f3f4f6;border:1px solid #e5e7eb;border-radius:8px;">
<tr><td style="padding:12px 16px;word-break:break-all;">
<a href="{{ACTIVATE_URL}}" style="font-family:ui-monospace,SFMono-Regular,'SF Mono',Menlo,Consolas,'Liberation Mono',monospace;font-size:12px;line-height:1.6;color:#4f46e5;text-decoration:none;">{{ACTIVATE_URL}}</a>
</td></tr>
</table>
<p style="margin:20px 0 0;font-size:12px;line-height:1.65;color:#9ca3af;">このメールに心当たりがない場合は、管理者までご連絡ください。</p>
</td></tr>
<tr><td align="center" style="padding:24px 16px 8px;">
<p style="margin:0;font-size:11px;color:#9ca3af;">© {{YEAR}} {{APP_NAME}}</p>
</td></tr>
</table>
</td></tr>
</table>
</body>
</html>`
