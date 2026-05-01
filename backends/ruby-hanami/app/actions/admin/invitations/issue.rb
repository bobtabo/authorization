# frozen_string_literal: true
#
# 招待発行アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Admin
      module Invitations
        # 招待を新規発行するアクションです。
        # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
        class Issue < Authorization::Action
          include Authorization::Actions::Base

          # @param request [Hanami::Action::Request] リクエスト
          # @param response [Hanami::Action::Response] レスポンス
          # @return [void]
          def handle(request, response)
            v = transaction { container[:invitation_uc].issue }
            json_response(response, { found: true, url: v.url, display_url: v.display_url, token: v.token })
          end
        end
      end
    end
  end
end
