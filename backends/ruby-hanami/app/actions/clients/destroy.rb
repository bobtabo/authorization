# frozen_string_literal: true
#
# クライアント削除アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Clients
      # 指定IDのクライアントを削除するアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Destroy < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          executor_id = staff_id_from_cookie(request)
          transaction do
            container[:client_uc].destroy(
              ::UseCase::Client::DestroyDto.new(
                id:          request.params[:id].to_i,
                executor_id: executor_id,
                version:     request.params[:version].to_i,
              )
            )
          end
          json_response(response, {})
        rescue ::Domain::ConflictError => e
          json_response(response, { error: e.message }, status: 409)
        end
      end
    end
  end
end
