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
        </html>
      HTML
    end
  end
end
