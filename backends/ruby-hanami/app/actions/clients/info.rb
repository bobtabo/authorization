# frozen_string_literal: true
#
# クライアント情報返却アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Clients
      # 指定 identifier のクライアント情報を返すアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Info < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          vo = container[:client_uc].get_info(
            ::UseCase::Client::InfoDto.new(identifier: request.params[:identifier])
          )
          json_response(response, {
            identifier: vo.identifier,
            name:       vo.name,
            status:     vo.status,
          })
        rescue RuntimeError => e
          json_response(response, { error: e.message }, status: 404)
        end
      end
    end
  end
end
