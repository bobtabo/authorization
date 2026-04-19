import logging
import smtplib
from datetime import datetime
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

from app.config.settings import get_settings

logger = logging.getLogger(__name__)


_ENV_LABELS = {
    "local": "Local",
    "testing": "Test",
    "develop": "Develop",
    "staging": "Staging",
}


def _mail_subject(env: str, subject: str) -> str:
    label = _ENV_LABELS.get(env, "")
    return f"[{label}]{subject}" if label else subject


def send_access_token(to: str, client_name: str, token: str) -> None:
    if not to:
        return
    settings = get_settings()
    subject = _mail_subject(settings.app_env, f"【{settings.app_name}】アクセストークンのお知らせ")
    html = _build_html(client_name, token, settings.app_name)

    msg = MIMEMultipart("alternative")
    msg["Subject"] = subject
    msg["From"] = f"{settings.app_name} <{settings.mail_from_address}>"
    msg["To"] = to
    msg.attach(MIMEText(html, "html", "utf-8"))

    try:
        with smtplib.SMTP(settings.mail_host, settings.mail_port) as smtp:
            if settings.mail_username:
                smtp.login(settings.mail_username, settings.mail_password)
            smtp.sendmail(settings.mail_from_address, [to], msg.as_string())
    except Exception as e:
        logger.error("mail send error: %s", e)


def _build_html(name: str, token: str, app_name: str) -> str:
    year = datetime.now().year
    return f"""<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{app_name} — アクセストークン</title>
</head>
<body style="margin:0;padding:0;background-color:#f9fafb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f9fafb;">
<tr><td align="center" style="padding:40px 16px;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="max-width:560px;margin:0 auto;">
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-bottom:none;border-radius:12px 12px 0 0;padding:20px 24px;">
<span style="font-size:15px;font-weight:600;color:#1f2937;">{app_name}</span>
</td></tr>
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-top:1px solid #f3f4f6;padding:28px 24px 32px;border-radius:0 0 12px 12px;">
<h1 style="margin:0 0 8px;font-size:18px;font-weight:600;color:#111827;">アクセストークンを発行しました</h1>
<p style="margin:0 0 20px;font-size:14px;color:#6b7280;">{name} 様</p>
<p style="margin:0 0 24px;font-size:14px;line-height:1.75;color:#374151;">
ご利用の <strong style="color:#4f46e5;">{app_name}</strong> 向けにアクセストークンを発行しました。<br>
以下のトークンは第三者に共有せず、安全な場所で保管してください。
</p>
<p style="margin:0 0 8px;font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;letter-spacing:0.06em;">発行されたトークン</p>
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f3f4f6;border:1px solid #e5e7eb;border-radius:8px;">
<tr><td style="padding:16px 18px;word-break:break-all;">
<code style="font-family:monospace;font-size:13px;color:#111827;">{token}</code>
</td></tr>
</table>
<p style="margin:20px 0 0;font-size:12px;color:#9ca3af;">このメールに心当たりがない場合は、管理者までご連絡ください。</p>
</td></tr>
<tr><td align="center" style="padding:24px 16px 8px;">
<p style="margin:0;font-size:11px;color:#9ca3af;">© {year} {app_name}</p>
</td></tr>
</table>
</td></tr>
</table>
</body>
</html>"""
