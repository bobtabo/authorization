# frozen_string_literal: true
#
# 招待トークン検証アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Auth
      # 招待トークンを検証し招待情報を返すアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Invitation < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          v = container[:invitation_uc].find_by_token(
            ::UseCase::Invitation::FindByTokenDto.new(token: request.params[:token])
          )
          json_response(response, { found: true, url: v.url, display_url: v.display_url, token: v.token })
        end
      end
    end
  end
end
