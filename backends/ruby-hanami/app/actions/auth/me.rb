# frozen_string_literal: true
#
# 認証ユーザー情報取得アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Auth
      # 現在認証中のスタッフ情報を返すアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Me < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          staff_id = staff_id_from_cookie(request)
          return json_response(response, { error: "unauthenticated" }, status: 401) if staff_id == 0

          s = container[:auth_uc].find_user(staff_id)
          json_response(response, { staff_id: s.id, name: s.name, avatar: s.avatar, role: s.role })
        end
      end
    end
  end
end
