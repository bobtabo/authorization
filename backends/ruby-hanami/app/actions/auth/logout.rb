# frozen_string_literal: true
#
# ログアウトアクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Auth
      # ログアウト処理を行うアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Logout < Authorization::Action
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
