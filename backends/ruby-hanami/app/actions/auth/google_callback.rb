# frozen_string_literal: true
#
# Google OAuth コールバックアクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "net/http"
require "json"
require "uri"

module Authorization
  module Actions
    module Auth
      # Google OAuth 認証後のコールバックを処理するアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class GoogleCallback < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          cfg  = container[:cfg]
          code = request.params[:code]

          return response.redirect_to "#{cfg.app.frontend_url}/error?code=400" if code.nil? || code.empty?

          begin
            token_data = exchange_code_for_token(code, cfg)
            user_info  = fetch_user_info(token_data["access_token"])

            dto = ::UseCase::Auth::LoginDto.new(
              provider:    ::Domain::Staff::Provider::GOOGLE,
              provider_id: user_info["id"],
              name:        user_info["name"],
              email:       user_info["email"],
              avatar:      user_info["picture"],
            )

            vo = transaction { container[:auth_uc].login(dto) }

            max_age     = cfg.app.staff_cookie_lifetime * 60
            secure_flag = cfg.app.env == "production" ? "; Secure" : ""
            response.headers["Set-Cookie"] =
              "staff_id=#{vo.id}; Path=/; HttpOnly; Max-Age=#{max_age}#{secure_flag}; SameSite=Lax"

            response.redirect_to "#{cfg.app.frontend_url}/clients"
          rescue => e
            warn "[google_callback] ERROR: #{e.class}: #{e.message}\n#{e.backtrace.first(5).join("\n")}"
            response.redirect_to "#{cfg.app.frontend_url}/error?code=500"
          end
        end

        private

        def exchange_code_for_token(code, cfg)
          uri  = URI("https://oauth2.googleapis.com/token")
          http = Net::HTTP.new(uri.host, uri.port)
          http.use_ssl = true
          req      = Net::HTTP::Post.new(uri.path, "Content-Type" => "application/x-www-form-urlencoded")
          req.body = URI.encode_www_form(
            client_id:     cfg.oauth.google_client_id,
            client_secret: cfg.oauth.google_client_secret,
            redirect_uri:  cfg.oauth.google_redirect_url,
            code:          code,
            grant_type:    "authorization_code",
          )
          res = http.request(req)
          raise "token_exchange_failed: #{res.body}" unless res.is_a?(Net::HTTPSuccess)
          JSON.parse(res.body)
        end

        def fetch_user_info(access_token)
          uri  = URI("https://www.googleapis.com/oauth2/v2/userinfo")
          http = Net::HTTP.new(uri.host, uri.port)
          http.use_ssl = true
          req = Net::HTTP::Get.new(uri.path)
          req["Authorization"] = "Bearer #{access_token}"
          res = http.request(req)
          raise "userinfo_fetch_failed: #{res.body}" unless res.is_a?(Net::HTTPSuccess)
          JSON.parse(res.body)
        end
      end
    end
  end
end
