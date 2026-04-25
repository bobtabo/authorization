# frozen_string_literal: true
#
# スタッフ削除アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Staffs
      # 指定スタッフを削除するアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Destroy < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          executor_id = staff_id_from_cookie(request)
          id = request.params[:id].to_i
          transaction do
            container[:staff_uc].destroy(
              UseCase::Staff::DestroyDto.new(id: id, executor_id: executor_id)
            )
          end
          json_response(response, { id: id })
        end
      end
    end
  end
end
