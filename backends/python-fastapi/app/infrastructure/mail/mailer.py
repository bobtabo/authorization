"""
メール送信インフラストラクチャー。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
import logging
from datetime import datetime

import boto3

from app.config.settings import get_settings

logger = logging.getLogger(__name__)


_ENV_LABELS = {
    "local": "Local",
    "testing": "Test",
    "develop": "Develop",
    "staging": "Staging",
}


def _mail_subject(env: str, subject: str) -> str:
    """環境ラベル付きメール件名を返します。

    Args:
        env: アプリケーション環境名
        subject: 件名

    Returns:
        ラベル付き件名文字列
    """
    label = _ENV_LABELS.get(env, "")
    return f"[{label}]{subject}" if label else subject


def _build_ses_client():
    """SES クライアントを生成します。"""
    settings = get_settings()
    kwargs = {
        "service_name": "ses",
        "region_name": settings.aws_region,
    }
    if settings.aws_access_key_id:
        kwargs["aws_access_key_id"] = settings.aws_access_key_id
        kwargs["aws_secret_access_key"] = settings.aws_secret_access_key
    if settings.aws_endpoint_url:
        kwargs["endpoint_url"] = settings.aws_endpoint_url
    return boto3.client(**kwargs)


def send_activation(to: str, client_name: str, activate_url: str) -> None:
    """クライアントに利用開始のご案内をメールで送信します。

    Args:
        to: 送信先メールアドレス
        client_name: クライアント名
        activate_url: アクティベートページURL
    """
    if not to:
        return
    settings = get_settings()
    subject = _mail_subject(settings.app_env, f"【{settings.app_name} / Python】ご利用開始のご案内")
    html = _build_html(client_name, activate_url, settings.app_name)

    try:
        client = _build_ses_client()
        client.send_email(
            Source=f"{settings.app_name} <{settings.mail_from_address}>",
            Destination={"ToAddresses": [to]},
            Message={
                "Subject": {"Data": subject, "Charset": "UTF-8"},
                "Body": {"Html": {"Data": html, "Charset": "UTF-8"}},
            },
        )
    except Exception as e:
        logger.error("mail send error: %s", e)


def _build_html(name: str, activate_url: str, app_name: str) -> str:
    """ご利用開始のご案内メールのHTML 本文を生成します。

    Args:
        name: クライアント名
        activate_url: アクティベートページURL
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
<body style="margin:0;padding:0;background-color:#f9fafb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,'Noto Sans JP','Hiragino Sans','Hiragino Kaku Gothic ProN',Meiryo,sans-serif;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f9fafb;">
<tr><td align="center" style="padding:40px 16px;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="max-width:560px;margin:0 auto;">
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-bottom:none;border-radius:12px 12px 0 0;padding:20px 24px;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%"><tr>
<td width="40" valign="middle" style="padding-right:12px;"><table role="presentation" cellpadding="0" cellspacing="0" border="0" style="width:36px;height:32px;background-color:#4f46e5;border-radius:8px;"><tr><td align="center" valign="middle"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.25 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/><path d="m9 12 2 2 4-4"/></svg></td></tr></table></td>
<td valign="middle"><span style="font-size:15px;font-weight:600;color:#1f2937;letter-spacing:-0.01em;">{app_name}</span></td>
</tr></table>
</td></tr>
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-top:1px solid #f3f4f6;padding:28px 24px 32px;border-radius:0 0 12px 12px;">
<h1 style="margin:0 0 8px;font-size:18px;font-weight:600;color:#111827;line-height:1.4;letter-spacing:-0.02em;">ご利用開始のご案内</h1>
<p style="margin:0 0 20px;font-size:14px;line-height:1.7;color:#6b7280;">{name} 様</p>
<p style="margin:0 0 24px;font-size:14px;line-height:1.75;color:#374151;">
<strong style="color:#4f46e5;font-weight:600;">{app_name}</strong> へのご登録が完了しました。<br>
以下のボタンからご利用を開始してください。
</p>
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%">
<tr><td align="center" style="padding:8px 0 24px;">
<a href="{activate_url}" style="display:inline-block;background-color:#4f46e5;color:#ffffff;font-size:14px;font-weight:600;text-decoration:none;padding:12px 32px;border-radius:8px;letter-spacing:0.01em;">ご利用を開始する</a>
</td></tr>
</table>
<p style="margin:0 0 8px;font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;letter-spacing:0.06em;">ボタンが開かない場合</p>
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f3f4f6;border:1px solid #e5e7eb;border-radius:8px;">
<tr><td style="padding:12px 16px;word-break:break-all;">
<a href="{activate_url}" style="font-family:ui-monospace,SFMono-Regular,'SF Mono',Menlo,Consolas,'Liberation Mono',monospace;font-size:12px;line-height:1.6;color:#4f46e5;text-decoration:none;">{activate_url}</a>
</td></tr>
</table>
<p style="margin:20px 0 0;font-size:12px;line-height:1.65;color:#9ca3af;">このメールに心当たりがない場合は、管理者までご連絡ください。</p>
</td></tr>
<tr><td align="center" style="padding:24px 16px 8px;">
<p style="margin:0;font-size:11px;color:#9ca3af;">© {year} {app_name}</p>
</td></tr>
</table>
</td></tr>
</table>
</body>
</html>"""
