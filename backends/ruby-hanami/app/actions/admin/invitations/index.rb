# frozen_string_literal: true
#
# 招待一覧アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Admin
      module Invitations
        # 現在有効な招待情報を返すアクションです。
        # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
        class Index < Authorization::Action
          include Authorization::Actions::Base

          # @param request [Hanami::Action::Request] リクエスト
          # @param response [Hanami::Action::Response] レスポンス
          # @return [void]
          def handle(request, response)
            v = container[:invitation_uc].current
            json_response(response, { found: true, url: v.url, display_url: v.display_url, token: v.token })
          end
        end
      end
    end
  end
end
