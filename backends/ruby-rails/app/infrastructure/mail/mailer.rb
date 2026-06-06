# frozen_string_literal: true
#
# メール送信モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "net/smtp"
require "time"

module Infrastructure
  module Mail
    # メール送信クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Mailer
      # @param cfg [MailConfig] メール設定
      def initialize(cfg = ConfigLoader.load.mail)
        @cfg = cfg
      end

      # クライアントへご利用開始のご案内メールを送信します。
      # @param to [String] 宛先メールアドレス
      # @param client_name [String] クライアント名
      # @param activate_url [String] QRページURL
      # @return [void]
      def send_activation(to, client_name, activate_url)
        return if to.nil? || to.empty?

        subject = mail_subject("【#{@cfg.app_name} / Ruby on Rails】ご利用開始のご案内")
        body    = build_activation_html(client_name, activate_url)

        message = build_message(to, subject, body)
        smtp_send(to, message)
      rescue => e
        Rails.logger.error("mail send error: #{e.message}")
      end

      private

      # @param subject [String] 件名
      # @return [String] 環境プレフィックス付き件名
      def mail_subject(subject)
        label = env_label(@cfg.app_env)
        label.empty? ? subject : "[#{label}]#{subject}"
      end

      # @param env [String] 環境名
      # @return [String] 環境ラベル
      def env_label(env)
        { "local" => "Local", "testing" => "Test", "develop" => "Develop", "staging" => "Staging" }.fetch(env, "")
      end

      # @param to [String] 宛先メールアドレス
      # @param subject [String] 件名
      # @param body [String] 本文 HTML
      # @return [String] MIME メッセージ
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

      # @param to [String] 宛先メールアドレス
      # @param message [String] MIME メッセージ
      # @return [void]
      def smtp_send(to, message)
        Net::SMTP.start(@cfg.host, @cfg.port.to_i) do |smtp|
          smtp.auth_login(@cfg.username, @cfg.password) if @cfg.username && !@cfg.username.empty?
          smtp.send_message(message, @cfg.from_address, to)
        end
      end

      # @param name [String] クライアント名
      # @param activate_url [String] QRページURL
      # @return [String] HTML 文字列
      def build_activation_html(name, activate_url)
        year = Time.now.year
        ACTIVATION_TEMPLATE
          .gsub("{{NAME}}", name)
          .gsub("{{ACTIVATE_URL}}", activate_url)
          .gsub("{{APP_NAME}}", @cfg.app_name)
          .gsub("{{YEAR}}", year.to_s)
      end

      ACTIVATION_TEMPLATE = <<~HTML
        <!DOCTYPE html>
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
        <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%"><tr>
        <td width="40" valign="middle" style="padding-right:12px;"><table role="presentation" cellpadding="0" cellspacing="0" border="0" style="width:36px;height:32px;background-color:#4f46e5;border-radius:8px;"><tr><td align="center" valign="middle"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.25 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/><path d="m9 12 2 2 4-4"/></svg></td></tr></table></td>
        <td valign="middle"><span style="font-size:15px;font-weight:600;color:#1f2937;letter-spacing:-0.01em;">{{APP_NAME}}</span></td>
        </tr></table>
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
        </html>
      HTML
    end
  end
end
