# frozen_string_literal: true
#
# クライアント利用開始アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Clients
      # 利用開始処理を行い、アクセストークンを返すアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Start < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          vo = transaction do
            container[:client_uc].start(
              ::UseCase::Client::StartDto.new(identifier: request.params[:identifier])
            )
          end
          json_response(response, { access_token: vo.access_token })
        rescue Domain::ConflictError => e
          json_response(response, { error: e.message }, status: 409)
        rescue RuntimeError => e
          json_response(response, { error: e.message }, status: 404)
        rescue StandardError => e
          $stderr.puts "[start] #{e.class}: #{e.message}\n#{e.backtrace.first(5).join("\n")}"
          json_response(response, { error: "internal_server_error" }, status: 500)
        end
      end
    end
  end
end
