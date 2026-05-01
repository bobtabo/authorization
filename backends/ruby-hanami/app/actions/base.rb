# frozen_string_literal: true
#
# アクション共通処理モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    # アクション共通の処理を提供するモジュールです。
    module Base
      TIME_FORMAT = "%Y-%m-%d %H:%M"

      # @return [AppContainer] DI コンテナのインスタンス
      def container
        AppContainer.instance
      end

      # @param request [Hanami::Action::Request] リクエストオブジェクト
      # @return [Integer] Cookie から取得したスタッフ ID
      def staff_id_from_cookie(request)
        request.cookies["staff_id"].to_i
      end

      # @param response [Hanami::Action::Response] レスポンスオブジェクト
      # @param data [Hash] レスポンスデータ
      # @param status [Integer] HTTP ステータスコード
      # @return [void]
      def json_response(response, data, status: 200)
        response.status = status
        response.format = :json
        response.body   = data.to_json
      end

      # ROM トランザクションを実行します。
      # @return [Object] ブロックの戻り値
      def transaction(&block)
        container[:rom].gateways[:default].connection.transaction(&block)
      end
    end
  end
end
