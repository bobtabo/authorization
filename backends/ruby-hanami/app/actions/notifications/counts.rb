# frozen_string_literal: true
#
# 通知件数取得アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Notifications
      # スタッフの未読・総通知件数を返すアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Counts < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          staff_id = staff_id_from_cookie(request)
          return json_response(response, { error: "unauthenticated" }, status: 401) if staff_id == 0

          v = container[:notification_uc].counts(staff_id)
          json_response(response, { unread: v.unread, total: v.total })
        end
      end
    end
  end
end
