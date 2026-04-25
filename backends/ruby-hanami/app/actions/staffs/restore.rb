# frozen_string_literal: true
#
# スタッフ復元アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Staffs
      # 削除済みスタッフを復元するアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Restore < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          id = request.params[:id].to_i
          transaction { container[:staff_uc].restore(id) }
          json_response(response, { id: id })
        end
      end
    end
  end
end
