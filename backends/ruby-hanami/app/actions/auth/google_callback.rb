# frozen_string_literal: true
#
# Google OAuth コールバックアクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

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
          json_response(response, {})
        end
      end
    end
  end
end
