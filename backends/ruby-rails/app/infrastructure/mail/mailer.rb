# frozen_string_literal: true
#
# メール送信インフラストラクチャー。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "aws-sdk-ses"
require "erb"
require "time"

module Infrastructure
  module Mail
    # SES メール送信クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Mailer
      # @param mail_cfg [MailConfig] メール設定
      # @param aws_cfg [AwsConfig] AWS設定
      def initialize(mail_cfg = ConfigLoader.load.mail, aws_cfg = ConfigLoader.load.aws)
        @mail_cfg = mail_cfg
        @aws_cfg  = aws_cfg
      end

      # クライアントへ利用開始のご案内メールを送信します。
      # @param to [String] 宛先メールアドレス
      # @param client_name [String] クライアント名
      # @param activate_url [String] QRページURL
      # @return [void]
      def send_activation(to, client_name, activate_url)
        return if to.nil? || to.empty?

        subject = mail_subject("【#{@mail_cfg.app_name} / Ruby on Rails】ご利用開始のご案内")
        body    = build_activation_html(client_name, activate_url)

        ses_client.send_email(
          source: "#{@mail_cfg.app_name} <#{@mail_cfg.from_address}>",
          destination: { to_addresses: [to] },
          message: {
            subject: { data: subject, charset: "UTF-8" },
            body: { html: { data: body, charset: "UTF-8" } }
          }
        )
      rescue => e
        Rails.logger.error("mail send error: #{e.message}")
      end

      private

      # @return [Aws::SES::Client] SES クライアント
      def ses_client
        opts = { region: @aws_cfg.region }
        unless @aws_cfg.access_key.nil? || @aws_cfg.access_key.empty?
          opts[:credentials] = Aws::Credentials.new(@aws_cfg.access_key, @aws_cfg.secret_key)
        end
        unless @aws_cfg.endpoint.nil? || @aws_cfg.endpoint.empty?
          opts[:endpoint] = @aws_cfg.endpoint
        end
        Aws::SES::Client.new(opts)
      end

      # @param subject [String] 件名
      # @return [String] 環境プレフィックス付き件名
      def mail_subject(subject)
        label = env_label(@mail_cfg.app_env)
        label.empty? ? subject : "[#{label}]#{subject}"
      end

      # @param env [String] 環境名
      # @return [String] 環境ラベル
      def env_label(env)
        { "local" => "Local", "testing" => "Test", "develop" => "Develop", "staging" => "Staging" }.fetch(env, "")
      end

      # @param name [String] クライアント名
      # @param activate_url [String] QRページURL
      # @return [String] HTML 文字列
      def build_activation_html(name, activate_url)
        template = ERB.new(File.read(activation_template_path), trim_mode: "-")
        template.result_with_hash(
          name: name,
          activate_url: activate_url,
          app_name: @mail_cfg.app_name,
          year: Time.now.year
        )
      end

      # @return [String] 利用開始案内メールのERBテンプレートパス
      def activation_template_path
        File.join(__dir__, "templates", "activation.html.erb")
      end
    end
  end
end
