# frozen_string_literal: true
#
# 通知一覧アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Notifications
      # スタッフの通知ページを返すアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Index < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          staff_id = staff_id_from_cookie(request)
          return json_response(response, { error: "unauthenticated" }, status: 401) if staff_id == 0

          cursor = request.params[:cursor]
          limit  = request.params[:limit]&.to_i || container[:cfg].app.notification_default_limit
          page   = container[:notification_uc].list_page(staff_id, cursor, limit)
          json_response(response, {
            items:       page.items.map { |n|
              {
                id:           n.id,
                staff_id:     n.staff_id,
                message_type: n.message_type,
                title:        n.title,
                message:      n.message,
                url:          n.url,
                read:         n.read,
                created_at:   n.created_at.strftime(TIME_FORMAT),
                updated_at:   n.updated_at.strftime(TIME_FORMAT),
              }
            },
            next_cursor: page.next_cursor,
          })
        end
      end
    end
  end
end
