require "net/smtp"
require "time"

module Infrastructure
  module Mail
    class Mailer
      def initialize(cfg = ConfigLoader.load)
        @cfg = cfg.mail
      end

      def send_access_token(to, client_name, token)
        return if to.nil? || to.empty?

        subject = mail_subject("【#{@cfg.app_name} / Ruby on Rails】アクセストークンのお知らせ")
        body    = build_access_token_html(client_name, token)

        message = build_message(to, subject, body)
        smtp_send(to, message)
      rescue => e
        Rails.logger.error("mail send error: #{e.message}")
      end

      private

      def mail_subject(subject)
        label = env_label(@cfg.app_env)
        label.empty? ? subject : "[#{label}]#{subject}"
      end

      def env_label(env)
        { "local" => "Local", "testing" => "Test", "develop" => "Develop", "staging" => "Staging" }.fetch(env, "")
      end

      def build_message(to, subject, body)
        encoded_subject = "=?UTF-8?B?#{Base64.strict_encode64(subject)}?="
        encoded_from    = "=?UTF-8?B?#{Base64.strict_encode64(@cfg.app_name)}?= <#{@cfg.from_address}>"

        "MIME-Version: 1.0\r\n" \
        "Content-Type: text/html; charset=UTF-8\r\n" \
        "From: #{encoded_from}\r\n" \
        "To: #{to}\r\n" \
        "Subject: #{encoded_subject}\r\n" \
        "\r\n#{body}"
      end

      def smtp_send(to, message)
        Net::SMTP.start(@cfg.host, @cfg.port.to_i) do |smtp|
          smtp.auth_login(@cfg.username, @cfg.password) if @cfg.username && !@cfg.username.empty?
          smtp.send_message(message, @cfg.from_address, to)
        end
      end

      def build_access_token_html(name, token)
        year = Time.now.year
        ACCESS_TOKEN_TEMPLATE
          .gsub("{{NAME}}", name)
          .gsub("{{TOKEN}}", token)
          .gsub("{{APP_NAME}}", @cfg.app_name)
          .gsub("{{YEAR}}", year.to_s)
      end

      ACCESS_TOKEN_TEMPLATE = <<~HTML
        <!DOCTYPE html>
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
        </html>
      HTML
    end
  end
end
