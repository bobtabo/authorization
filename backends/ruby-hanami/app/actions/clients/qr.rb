# frozen_string_literal: true
#
# クライアント QR コードデータ返却アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Clients
      # 指定 identifier のクライアント QR コードデータを返すアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Qr < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          vo = container[:client_uc].get_qr(
            ::UseCase::Client::QrDto.new(identifier: request.params[:identifier])
          )
          json_response(response, {
            identifier:   vo.identifier,
            deeplink_url: vo.deeplink_url,
          })
        rescue RuntimeError => e
          json_response(response, { error: e.message }, status: 404)
        end
      end
    end
  end
end
