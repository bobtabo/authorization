<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Authorization Gateway — アクセストークン</title>
    <!--[if mso]>
    <noscript>
        <xml>
            <o:OfficeDocumentSettings>
                <o:PixelsPerInch>96</o:PixelsPerInch>
            </o:OfficeDocumentSettings>
        </xml>
    </noscript>
    <![endif]-->
</head>
<body style="margin:0;padding:0;background-color:#f9fafb;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,'Noto Sans JP','Hiragino Sans','Hiragino Kaku Gothic ProN',Meiryo,sans-serif;">
    <div style="display:none;max-height:0;overflow:hidden;mso-hide:all;">
        アクセストークンを発行しました。Authorization Gateway
    </div>
    <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f9fafb;">
        <tr>
            <td align="center" style="padding:40px 16px;">
                <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="max-width:560px;margin:0 auto;">
                    <!-- ヘッダー-->
                    <tr>
                        <td style="background-color:#ffffff;border:1px solid #e5e7eb;border-bottom:none;border-radius:12px 12px 0 0;padding:20px 24px;">
                            <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td width="40" valign="middle" style="padding-right:12px;">
                                        <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="width:36px;height:32px;background-color:#4f46e5;border-radius:8px;">
                                            <tr>
                                                <td align="center" valign="middle" style="font-size:16px;line-height:32px;color:#ffffff;font-weight:700;">✓</td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td valign="middle">
                                        <span style="font-size:15px;font-weight:600;color:#1f2937;letter-spacing:-0.01em;">Authorization Gateway</span>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <!-- メイン -->
                    <tr>
                        <td style="background-color:#ffffff;border:1px solid #e5e7eb;border-top:1px solid #f3f4f6;padding:28px 24px 32px;border-radius:0 0 12px 12px;">
                            <h1 style="margin:0 0 8px;font-size:18px;font-weight:600;color:#111827;line-height:1.4;letter-spacing:-0.02em;">
                                アクセストークンを発行しました
                            </h1>
                            <p style="margin:0 0 20px;font-size:14px;line-height:1.7;color:#6b7280;">
                                {{ $name }} 様
                            </p>
                            <p style="margin:0 0 24px;font-size:14px;line-height:1.75;color:#374151;">
                                ご利用の <strong style="color:#4f46e5;font-weight:600;">Authorization Gateway</strong> 向けにアクセストークンを発行しました。<br>
                                以下のトークンは第三者に共有せず、安全な場所で保管してください。
                            </p>
                            <p style="margin:0 0 8px;font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;letter-spacing:0.06em;">
                                発行されたトークン
                            </p>
                            <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background-color:#f3f4f6;border:1px solid #e5e7eb;border-radius:8px;">
                                <tr>
                                    <td style="padding:16px 18px;word-break:break-all;">
                                        <code style="font-family:ui-monospace,SFMono-Regular,'SF Mono',Menlo,Consolas,'Liberation Mono',monospace;font-size:13px;line-height:1.6;color:#111827;">{{ $accessToken }}</code>
                                    </td>
                                </tr>
                            </table>
                            <p style="margin:20px 0 0;font-size:12px;line-height:1.65;color:#9ca3af;">
                                このメールに心当たりがない場合は、管理者までご連絡ください。
                            </p>
                        </td>
                    </tr>
                    <!-- フッター -->
                    <tr>
                        <td align="center" style="padding:24px 16px 8px;">
                            <p style="margin:0;font-size:11px;line-height:1.6;color:#9ca3af;">
                                © {{ now()->year }} Authorization Gateway
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
