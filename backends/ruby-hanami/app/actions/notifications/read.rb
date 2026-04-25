# frozen_string_literal: true
#
# 通知既読アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Notifications
      # 指定IDの通知を既読にするアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Read < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          id = request.params[:id].to_i
          transaction { container[:notification_uc].mark_read(id) }
          json_response(response, { id: id })
        end
      end
    end
  end
end
