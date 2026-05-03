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
          transaction do
            container[:staff_uc].restore(
              ::UseCase::Staff::RestoreDto.new(
                id:      id,
                version: request.params[:version].to_i,
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
