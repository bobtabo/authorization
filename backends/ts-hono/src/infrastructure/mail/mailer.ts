/**
 * メール送信インフラモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import nodemailer from "nodemailer";
import { config } from "../../config.js";

const ENV_LABELS: Record<string, string> = {
  local: "Local",
  testing: "Test",
  develop: "Develop",
  staging: "Staging",
};

function mailSubject(subject: string): string {
  const label = ENV_LABELS[config.app.env] ?? "";
  return label ? `[${label}]${subject}` : subject;
}

/**
 * クライアントにご利用開始のご案内をメール送信します。
 * @param to - 送信先メールアドレス
 * @param clientName - クライアント名
 * @param activateUrl - QRページURL
 */
export async function sendActivation(to: string, clientName: string, activateUrl: string): Promise<void> {
  if (!to) return;

  const { host, port, username, password, fromAddress, appName } = config.mail;
  const subject = mailSubject(`【${appName} / TypeScript】ご利用開始のご案内`);
  const html = buildHtml(clientName, activateUrl, appName);

  const transporter = nodemailer.createTransport({
    host,
    port,
    secure: false,
    ...(username ? { auth: { user: username, pass: password } } : {}),
  });

  try {
    await transporter.sendMail({
      from: `"${appName}" <${fromAddress}>`,
      to,
      subject,
      html,
    });
  } catch (err) {
    console.error("mail send error:", err);
  }
}

function buildHtml(name: string, activateUrl: string, appName: string): string {
  const year = new Date().getFullYear();
  return `<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${appName} — ご利用開始のご案内</title>
</head>
<body style="margin:0;padding:0;background-color:#f9fafb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f9fafb;">
<tr><td align="center" style="padding:40px 16px;">
<table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="max-width:560px;margin:0 auto;">
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-bottom:none;border-radius:12px 12px 0 0;padding:20px 24px;">
<span style="font-size:15px;font-weight:600;color:#1f2937;">${appName}</span>
</td></tr>
<tr><td style="background-color:#ffffff;border:1px solid #e5e7eb;border-top:1px solid #f3f4f6;padding:28px 24px 32px;border-radius:0 0 12px 12px;">
<h1 style="margin:0 0 8px;font-size:18px;font-weight:600;color:#111827;">ご利用開始のご案内</h1>
<p style="margin:0 0 20px;font-size:14px;color:#6b7280;">${name} 様</p>
<p style="margin:0 0 24px;font-size:14px;line-height:1.75;color:#374151;">
この度は <strong style="color:#4f46e5;">${appName}</strong> にご登録いただきありがとうございます。<br>
下のボタンからご利用を開始してください。
</p>
<table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 24px;">
<tr><td style="border-radius:6px;background-color:#4f46e5;">
<a href="${activateUrl}" target="_blank" style="display:inline-block;padding:12px 28px;font-size:14px;font-weight:600;color:#ffffff;text-decoration:none;border-radius:6px;">ご利用を開始する</a>
</td></tr>
</table>
<p style="margin:0 0 8px;font-size:12px;color:#6b7280;">ボタンが機能しない場合は以下の URL をブラウザに貼り付けてください。</p>
<p style="margin:0 0 0;font-size:12px;color:#6b7280;word-break:break-all;">${activateUrl}</p>
<p style="margin:20px 0 0;font-size:12px;color:#9ca3af;">このメールに心当たりがない場合は、管理者までご連絡ください。</p>
</td></tr>
<tr><td align="center" style="padding:24px 16px 8px;">
<p style="margin:0;font-size:11px;color:#9ca3af;">© ${year} ${appName}</p>
</td></tr>
</table>
</td></tr>
</table>
</body>
</html>`;
}
