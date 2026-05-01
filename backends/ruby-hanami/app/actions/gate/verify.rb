# frozen_string_literal: true
#
# ゲートトークン検証アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Gate
      # ゲートJWTを検証しクレームを返すアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Verify < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          token = request.params[:token]
          if token.nil? || token.to_s.empty?
            return json_response(response, { error: "token_required" }, status: 400)
          end

          v = container[:gate_uc].verify(
            ::UseCase::Gate::VerifyDto.new(
              identifier: request.params[:identifier],
              token:      token,
            )
          )
          json_response(response, v.claims)
        end
      end
    end
  end
end
