# frozen_string_literal: true
#
# クライアント削除アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Clients
      # 指定IDのクライアントを削除するアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Destroy < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          executor_id = staff_id_from_cookie(request)
          transaction { container[:client_uc].destroy(request.params[:id].to_i, executor_id) }
          json_response(response, {})
        end
      end
    end
  end
end
