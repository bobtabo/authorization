//! メール送信インフラモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use crate::config::MailConfig;
use chrono::Datelike;
use lettre::message::header::ContentType;
use lettre::transport::smtp::authentication::Credentials;
use lettre::{AsyncSmtpTransport, AsyncTransport, Message, Tokio1Executor};
use tracing::error;

pub struct Mailer {
    cfg: MailConfig,
}

impl Mailer {
    pub fn new(cfg: MailConfig) -> Self {
        Self { cfg }
    }

    pub async fn send_activation(&self, to: &str, client_name: &str, activate_url: &str) {
        if to.is_empty() {
            return;
        }

        let subject = mail_subject(&self.cfg.app_env, &format!("【{} / Rust】ご利用開始のご案内", self.cfg.app_name));
        let body = build_activation_html(client_name, activate_url, &self.cfg.app_name);

        let from = format!("\"{}\" <{}>", self.cfg.app_name, self.cfg.from_address);
        let email = match Message::builder()
            .from(from.parse().unwrap())
            .to(to.parse().unwrap())
            .subject(&subject)
            .header(ContentType::TEXT_HTML)
            .body(body)
        {
            Ok(e) => e,
            Err(e) => { error!("mail build error: {e}"); return; }
        };

        let addr = format!("{}:{}", self.cfg.host, self.cfg.port);
        let transport: AsyncSmtpTransport<Tokio1Executor> = if self.cfg.username.is_empty() {
            AsyncSmtpTransport::<Tokio1Executor>::builder_dangerous(&addr)
                .build()
        } else {
            let creds = Credentials::new(self.cfg.username.clone(), self.cfg.password.clone());
            AsyncSmtpTransport::<Tokio1Executor>::relay(&self.cfg.host)
                .unwrap()
                .credentials(creds)
                .build()
        };

        if let Err(e) = transport.send(email).await {
            error!("mail send error: {e}");
        }
    }
}

fn env_label(env: &str) -> &str {
    match env {
        "local"   => "Local",
        "testing" => "Test",
        "develop" => "Develop",
        "staging" => "Staging",
        _         => "",
    }
}

fn mail_subject(env: &str, subject: &str) -> String {
    let label = env_label(env);
    if label.is_empty() {
        subject.to_string()
    } else {
        format!("[{}]{}", label, subject)
    }
}

fn build_activation_html(name: &str, activate_url: &str, app_name: &str) -> String {
    let year = chrono::Local::now().year();
    ACTIVATION_TEMPLATE
        .replace("{{NAME}}", name)
        .replace("{{ACTIVATE_URL}}", activate_url)
        .replace("{{APP_NAME}}", app_name)
        .replace("{{YEAR}}", &year.to_string())
}

const ACTIVATION_TEMPLATE: &str = r#"<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{{APP_NAME}} — ご利用開始のご案内</title>
</head>
<body style="margin:0;padding:0;background-color:#f9fafb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f9fafb;">
<tr><td align="center" style="padding:40px 16px;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="max-width:560px;margin:0 auto;">
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-bottom:none;border-radius:12px 12px 0 0;padding:20px 24px;">
<span style="font-size:15px;font-weight:600;color:#1f2937;">{{APP_NAME}}</span>
</td></tr>
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-top:1px solid #f3f4f6;padding:28px 24px 32px;border-radius:0 0 12px 12px;">
<h1 style="margin:0 0 8px;font-size:18px;font-weight:600;color:#111827;">ご利用開始のご案内</h1>
<p style="margin:0 0 20px;font-size:14px;color:#6b7280;">{{NAME}} 様</p>
<p style="margin:0 0 24px;font-size:14px;line-height:1.75;color:#374151;">
この度は <strong style="color:#4f46e5;">{{APP_NAME}}</strong> にご登録いただきありがとうございます。<br>
下のボタンからご利用を開始してください。
</p>
<table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 24px;">
<tr><td style="border-radius:6px;background-color:#4f46e5;">
<a href="{{ACTIVATE_URL}}" target="_blank" style="display:inline-block;padding:12px 28px;font-size:14px;font-weight:600;color:#ffffff;text-decoration:none;border-radius:6px;">ご利用を開始する</a>
</td></tr>
</table>
<p style="margin:0 0 8px;font-size:12px;color:#6b7280;">ボタンが機能しない場合は以下の URL をブラウザに貼り付けてください。</p>
<p style="margin:0 0 0;font-size:12px;color:#6b7280;word-break:break-all;">{{ACTIVATE_URL}}</p>
<p style="margin:20px 0 0;font-size:12px;color:#9ca3af;">このメールに心当たりがない場合は、管理者までご連絡ください。</p>
</td></tr>
<tr><td align="center" style="padding:24px 16px 8px;">
<p style="margin:0;font-size:11px;color:#9ca3af;">© {{YEAR}} {{APP_NAME}}</p>
</td></tr>
</table>
</td></tr>
</table>
</body>
</html>"#;
