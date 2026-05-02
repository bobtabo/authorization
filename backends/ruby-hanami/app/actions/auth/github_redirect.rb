# frozen_string_literal: true
#
# GitHub OAuth リダイレクトアクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "cgi"

module Authorization
  module Actions
    module Auth
      # GitHub OAuth 認証ページへリダイレクトするアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class GithubRedirect < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          cfg   = container[:cfg]
          token = request.params[:token]
          state = (token && !token.empty?) ? "#{cfg.app.runtime}|#{token}" : cfg.app.runtime
          url = "https://github.com/login/oauth/authorize" \
                "?client_id=#{cfg.oauth.github_client_id}" \
                "&redirect_uri=#{CGI.escape(cfg.oauth.github_redirect_url)}" \
                "&scope=user:email" \
                "&state=#{CGI.escape(state)}"
          response.redirect_to url
        end
      end
    end
  end
end
