"""
メール送信モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
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
    """環境ラベルを付与したメール件名を返します。

    Args:
        env: アプリケーション環境名
        subject: 件名

    Returns:
        ラベル付き件名文字列
    """
    label = _ENV_LABELS.get(env, "")
    return f"[{label}]{subject}" if label else subject


def send_activation(to: str, client_name: str, activate_url: str) -> None:
    """クライアントにご利用開始のご案内をメール送信します。

    Args:
        to: 送信先メールアドレス
        client_name: クライアント名
        activate_url: QRページURL
    """
    if not to:
        return
    settings = get_settings()
    subject = _mail_subject(settings.app_env, f"【{settings.app_name} / Python】ご利用開始のご案内")
    html = _build_html(client_name, activate_url, settings.app_name)

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


def _build_html(name: str, activate_url: str, app_name: str) -> str:
    """ご利用開始のご案内メールの HTML 本文を生成します。

    Args:
        name: クライアント名
        activate_url: QRページURL
        app_name: アプリケーション名

    Returns:
        HTML 文字列
    """
    year = datetime.now().year
    return f"""<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{app_name} — ご利用開始のご案内</title>
</head>
<body style="margin:0;padding:0;background-color:#f9fafb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f9fafb;">
<tr><td align="center" style="padding:40px 16px;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="max-width:560px;margin:0 auto;">
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-bottom:none;border-radius:12px 12px 0 0;padding:20px 24px;">
<span style="font-size:15px;font-weight:600;color:#1f2937;">{app_name}</span>
</td></tr>
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-top:1px solid #f3f4f6;padding:28px 24px 32px;border-radius:0 0 12px 12px;">
<h1 style="margin:0 0 8px;font-size:18px;font-weight:600;color:#111827;">ご利用開始のご案内</h1>
<p style="margin:0 0 20px;font-size:14px;color:#6b7280;">{name} 様</p>
<p style="margin:0 0 24px;font-size:14px;line-height:1.75;color:#374151;">
この度は <strong style="color:#4f46e5;">{app_name}</strong> にご登録いただきありがとうございます。<br>
下のボタンからご利用を開始してください。
</p>
<table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 24px;">
<tr><td style="border-radius:6px;background-color:#4f46e5;">
<a href="{activate_url}" target="_blank" style="display:inline-block;padding:12px 28px;font-size:14px;font-weight:600;color:#ffffff;text-decoration:none;border-radius:6px;">ご利用を開始する</a>
</td></tr>
</table>
<p style="margin:0 0 8px;font-size:12px;color:#6b7280;">ボタンが機能しない場合は以下の URL をブラウザに貼り付けてください。</p>
<p style="margin:0 0 0;font-size:12px;color:#6b7280;word-break:break-all;">{activate_url}</p>
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
