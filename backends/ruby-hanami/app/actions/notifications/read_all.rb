# frozen_string_literal: true
#
# 通知一括既読アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Notifications
      # スタッフの全通知を既読にするアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class ReadAll < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          staff_id = staff_id_from_cookie(request)
          return json_response(response, { error: "unauthenticated" }, status: 401) if staff_id == 0

          updated = transaction { container[:notification_uc].bulk_mark_read(staff_id) }
          json_response(response, { updated: updated })
        end
      end
    end
  end
end
