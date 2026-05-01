# frozen_string_literal: true
#
# Google OAuth リダイレクトアクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "cgi"

module Authorization
  module Actions
    module Auth
      # Google OAuth 認証ページへリダイレクトするアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class GoogleRedirect < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          cfg   = container[:cfg]
          token = request.params[:token]
          state = (token && !token.empty?) ? token : "state"
          url = "https://accounts.google.com/o/oauth2/auth" \
                "?client_id=#{cfg.oauth.google_client_id}" \
                "&redirect_uri=#{CGI.escape(cfg.oauth.google_redirect_url)}" \
                "&response_type=code&scope=email+profile&access_type=online" \
                "&state=#{CGI.escape(state)}"
          response.redirect_to url
        end
      end
    end
  end
end
