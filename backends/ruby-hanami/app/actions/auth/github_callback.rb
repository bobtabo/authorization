# frozen_string_literal: true
#
# GitHub OAuth コールバックアクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "net/http"
require "json"
require "uri"

module Authorization
  module Actions
    module Auth
      # GitHub OAuth 認証後のコールバックを処理するアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class GithubCallback < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          cfg  = container[:cfg]
          code = request.params[:code]

          return response.redirect_to "#{cfg.app.frontend_url}/error?code=400" if code.nil? || code.empty?

          state_val        = request.params[:state].to_s
          parts            = state_val.split("|", 2)
          invitation_token = parts.length == 2 ? parts[1] : nil

          begin
            token_data   = exchange_code_for_token(code, cfg)
            access_token = token_data["access_token"]
            user_info    = fetch_user_info(access_token)
            email        = fetch_primary_email(access_token)
            name         = user_info["name"].to_s.empty? ? user_info["login"] : user_info["name"]

            dto = ::UseCase::Auth::LoginDto.new(
              provider:         ::Domain::Staff::Provider::GITHUB,
              provider_id:      user_info["id"].to_s,
              name:             name,
              email:            email,
              avatar:           user_info["avatar_url"],
              invitation_token: invitation_token,
            )

            vo = transaction { container[:auth_uc].login(dto) }

            max_age     = cfg.app.staff_cookie_lifetime * 60
            secure_flag = cfg.app.env == "production" ? "; Secure" : ""
            response.headers["Set-Cookie"] =
              "staff_id=#{vo.id}; Path=/; HttpOnly; Max-Age=#{max_age}#{secure_flag}; SameSite=Lax"

            response.redirect_to "#{cfg.app.frontend_url}/clients"
          rescue ::Domain::ForbiddenError
            response.redirect_to "#{cfg.app.frontend_url}/error?code=403"
          rescue => e
            warn "[github_callback] ERROR: #{e.class}: #{e.message}\n#{e.backtrace.first(5).join("\n")}"
            response.redirect_to "#{cfg.app.frontend_url}/error?code=500"
          end
        end

        private

        def exchange_code_for_token(code, cfg)
          uri  = URI("https://github.com/login/oauth/access_token")
          http = Net::HTTP.new(uri.host, uri.port)
          http.use_ssl = true
          req      = Net::HTTP::Post.new(uri.path, "Content-Type" => "application/x-www-form-urlencoded", "Accept" => "application/json")
          req.body = URI.encode_www_form(
            client_id:     cfg.oauth.github_client_id,
            client_secret: cfg.oauth.github_client_secret,
            redirect_uri:  cfg.oauth.github_redirect_url,
            code:          code,
          )
          res = http.request(req)
          raise "token_exchange_failed: #{res.body}" unless res.is_a?(Net::HTTPSuccess)
          JSON.parse(res.body)
        end

        def fetch_user_info(access_token)
          uri  = URI("https://api.github.com/user")
          http = Net::HTTP.new(uri.host, uri.port)
          http.use_ssl = true
          req = Net::HTTP::Get.new(uri.path)
          req["Authorization"] = "Bearer #{access_token}"
          req["Accept"]        = "application/json"
          res = http.request(req)
          raise "userinfo_fetch_failed: #{res.body}" unless res.is_a?(Net::HTTPSuccess)
          JSON.parse(res.body)
        end

        def fetch_primary_email(access_token)
          uri  = URI("https://api.github.com/user/emails")
          http = Net::HTTP.new(uri.host, uri.port)
          http.use_ssl = true
          req = Net::HTTP::Get.new(uri.path)
          req["Authorization"] = "Bearer #{access_token}"
          req["Accept"]        = "application/json"
          res = http.request(req)
          raise "email_fetch_failed: #{res.body}" unless res.is_a?(Net::HTTPSuccess)
          emails = JSON.parse(res.body)
          primary = emails.find { |e| e["primary"] == true }
          primary ? primary["email"] : emails.first&.dig("email")
        end
      end
    end
  end
end
