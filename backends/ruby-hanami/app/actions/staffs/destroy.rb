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
              ::UseCase::Staff::DestroyDto.new(
                id:          id,
                executor_id: executor_id,
                version:     request.params[:version].to_i,
              )
            )
          end
          json_response(response, { id: id })
        rescue ::Domain::ConflictError => e
          json_response(response, { error: e.message }, status: 409)
        end
      end
    end
  end
end
